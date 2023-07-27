package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.dao.ProductAttrValueDao;
import com.mall.product.entity.ProductAttrValueEntity;
import com.mall.product.service.ProductAttrValueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     *  SpuInfoServiceImpl 方法：saveSpuInfo-> 保存spu的规格参数 同时需要操作另一张表:pms_product_attr_value
     */
    @Override
    public void saveProductAttrValue(List<ProductAttrValueEntity> list) {
        this.saveBatch(list);
    }

    /**
     * 查询属性规格
     */
    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        List<ProductAttrValueEntity> entities = this.baseMapper.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        return entities;
    }

    /**
     * 更改规格参数：参数名、参数id、参数、状态的一一对应
     */
    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        // 1.删除 spuId 之前对应的属性
        this.baseMapper.delete(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        // 2.保存页面传过来的数据
        List<ProductAttrValueEntity> collect = entities.stream().map(entity -> {
            entity.setSpuId(spuId);
            entity.setAttrSort(0);
            return entity;
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

}