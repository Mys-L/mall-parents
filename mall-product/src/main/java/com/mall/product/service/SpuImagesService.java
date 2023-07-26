package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.SpuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * spu图片
 *
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:53:37
 */
public interface SpuImagesService extends IService<SpuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveImages(Long id, List<String> images);
}

