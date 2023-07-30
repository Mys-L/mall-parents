package com.mall.product.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.constant.Constant;
import com.mall.common.to.SkuReductionTo;
import com.mall.common.to.SpuBoundTo;
import com.mall.common.to.elasticsearch.SkuElasticModel;
import com.mall.common.to.elasticsearch.SkuHasStockVo;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.common.utils.R;
import com.mall.product.dao.SpuInfoDao;
import com.mall.product.entity.*;
import com.mall.product.feign.CouponFeignService;
import com.mall.product.feign.ElasticSearchFeignService;
import com.mall.product.feign.WareFeignService;
import com.mall.product.service.*;
import com.mall.product.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


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
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    ElasticSearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * @author: L
     * @description: TODO 高级部分完善
     * @date: 16:07:37
     * @param: vo

     * TODO:
     **/
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
                }).filter(entity->{
                    //返回true就是需要，否则剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
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
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode()!=0){
                        log.error("远程保存sku的优惠满减等信息失败！");
                    }
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

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }


    /**
     * 商品上架
     * 不一样的属性：skuPrice、skuImg、hasStock、hotScore、
     * brandName、brandImg、catalogName、attrs
     * spuId-> 商品和属性关联
     * productAttrValue s -> attrIds -> attrs ->过滤 -> SkuEsModel.Attrs -> skuIds->
     */
    @Override
    public void up(Long spuId) {
        // 1 组装数据 查出当前spuId对应的所有sku信息
        //1.1首先 查出sku信息
        List<SkuInfoEntity> skuEntities= skuInfoService.getSkuBySpuId(spuId);
        // 2.4 查出当前sku所有可以被用来检索规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).toList();
        // 过滤出可被检索的基本属性id，即search_type = 1，并且转化成elasticsearch模型
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuElasticModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            //组装数据
            SkuElasticModel.Attrs attrs = new SkuElasticModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).toList();

        // TODO 2.1 远程调用 查询是否库存
        List<Long> skuIds = skuEntities.stream().map(SkuInfoEntity::getSkuId).toList();
        Map<Long, Boolean> isHasStock=new HashMap<>();
        try {
            R skuHasStock = wareFeignService.getSkuHasStock(skuIds);
            isHasStock = skuHasStock.getData(new TypeReference<List<SkuHasStockVo>>() {})
                    .stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            log.debug("服务调用成功!" + isHasStock);
        }catch (Exception e) {
            log.error("库存服务调用失败: 原因{}", e);
        }
        Map<Long, Boolean> finalCollect = isHasStock;
        // 2 封装上架sku信息
        List<SkuElasticModel> upProduct = skuEntities.stream().map(sku -> {
            SkuElasticModel skuElasticModel = new SkuElasticModel();
            //拷贝相同信息
            BeanUtils.copyProperties(sku, skuElasticModel);
            //设置不同信息
            skuElasticModel.setSkuPrice(sku.getPrice());
            skuElasticModel.setSkuImg(sku.getSkuDefaultImg());
            //设置库存，只查是否有库存，不查有多少(2.1 远程调用)
            if (null == finalCollect) {
                skuElasticModel.setHasStock(true);
            } else {
                skuElasticModel.setHasStock(finalCollect.get(sku.getSkuId()));
            }
            // 2.2 热度评分 默认0
            skuElasticModel.setHotScore(0L);
            // 2.3 查询品牌和分类名字
            BrandEntity brandEntity = brandService.getById(skuElasticModel.getBrandId());
            skuElasticModel.setBrandName(brandEntity.getName());
            skuElasticModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(skuElasticModel.getCatalogId());
            skuElasticModel.setCatalogName(categoryEntity.getName());
            skuElasticModel.setAttrs(attrsList);
            return skuElasticModel;
        }).toList();
        // TODO 3 最终发给elasticsearch进行保存 mall-search
        R r = searchFeignService.productStatusUp(upProduct);
        if (r.getCode()==0){
            //远程调用成功 成功之后修改当前商品sku状态
            this.baseMapper.updateSpuStatus(spuId,Constant.StatusEnum.SPU_UP.getCode());
        }else {
            //远程调用失败
            // TODO 存在问题 接口幂等性,重复调用等问题
        }
    }

}