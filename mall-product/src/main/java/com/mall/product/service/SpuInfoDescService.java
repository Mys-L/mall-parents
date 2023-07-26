package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:53:37
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     *  SpuInfoServiceImpl方法：saveSpuInfo-> 2 保存spu的描述图片 pms_spu_info_desc
     */
    void saveSpuInfoDesc(SpuInfoDescEntity descEntity);
}

