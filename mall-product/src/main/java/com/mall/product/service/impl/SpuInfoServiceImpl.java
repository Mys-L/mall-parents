package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.to.SkuReductionTo;
import com.mall.common.to.SpuBoundTo;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.product.dao.SpuInfoDao;
import com.mall.product.entity.*;
import com.mall.product.feign.CouponFeignService;
import com.mall.product.service.*;
import com.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1 保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //2 保存spu的描述图片 pms_spu_info_desc 调用 spuInfoDescService
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //3 保存spu的图片集合 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

        //4 保存spu的规格参数 同时需要操作另一张表:pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> list = baseAttrs.stream().map((attr) -> {
            ProductAttrValueEntity productAttrValue = new ProductAttrValueEntity();
            productAttrValue.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            productAttrValue.setAttrName(attrEntity.getAttrName());
            productAttrValue.setAttrValue(attr.getAttrValues());
            productAttrValue.setQuickShow(attr.getShowDesc());
            productAttrValue.setSpuId(spuInfoEntity.getId());
            return productAttrValue;
        }).toList();
        productAttrValueService.saveProductAttrValue(list);

        //5 保存spu的积分信息 跨数据库 mall_sms -> sms_spu_bounds
        //TODO 远程保存
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode()!=0){
            log.error("远程保存spu的积分信息失败！");
        }

        //6 保存当前spu对应的所有sku的信息
        List<Skus> skus = vo.getSkus();
        if (skus == null && skus.size()>0) {
            skus.forEach(item ->{
                String defaultImg ="";
                for (Images image: item.getImages()) {
                    if (image.getDefaultImg()==1){
                        defaultImg = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //6.1) 保存sku 的基本信息 pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> skuImagesList = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).toList();
                //6.2) 保存sku 的图片信息 pms_sku_images
                skuImagesService.saveBatch(skuImagesList);

                //6.3) 保存sku 的销售属性信息 pms_sku_sale_attr_value
                List<Attr> attrs = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueList = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValue = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValue);
                    skuSaleAttrValue.setSkuId(skuId);
                    return skuSaleAttrValue;
                }).toList();
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);

                //6.4) 保存sku 的优惠满减等信息 跨数据库保存mall_sms 数据库->sms_sku_ladder(打折) sms_sku_full_reduction(满减) sms_member_price(会员价格)
                //TODO 远程保存
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                if (r1.getCode()!=0){
                    log.error("远程保存sku的优惠满减等信息失败！");
                }

            });
        }

    }

    /**
     *  SpuInfoServiceImpl方法：saveSpuInfo-> 1 保存spu基本信息 pms_spu_info
     */
    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

}