package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;
import com.mall.product.dao.BrandDao;
import com.mall.product.entity.BrandEntity;
import com.mall.product.service.BrandService;
import com.mall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        String key =(String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("brand_id",key).or().like("name",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }
    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        //保证冗余数据字段的一致性
        this.updateById(brand);
        if (!StringUtils.isEmpty(brand.getName())) {
            //同步更新其它关联表信息
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
            //TODO 更新其它关联
        }
    }

    @Override
    public List<BrandEntity> getBrandByIds(List<Long> brandIds) {
        return this.baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id", brandIds));
    }

}