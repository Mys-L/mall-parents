package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.AttrAttrgroupRelationEntity;
import com.mall.product.vo.AttrGroupRelatinVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:53:37
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatch(List<AttrGroupRelatinVo> vos);
}

