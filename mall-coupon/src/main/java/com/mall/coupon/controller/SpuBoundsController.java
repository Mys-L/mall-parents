package com.mall.coupon.controller;

import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;
import com.mall.coupon.entity.SpuBoundsEntity;
import com.mall.coupon.service.SpuBoundsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 商品spu积分设置
 *
 * @author L
 * @email L@163.com
 * @date 2023-07-15 15:57:09
 */
@RestController
@RequestMapping("coupon/spubounds")
public class SpuBoundsController {
    @Autowired
    private SpuBoundsService spuBoundsService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuBoundsService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SpuBoundsEntity spuBounds = spuBoundsService.getById(id);

        return R.ok().put("spuBounds", spuBounds);
    }

    /**
     * 保存
     * SpuInfoServiceImpl 方法：saveSpuInfo-> 5 保存spu的积分信息 sms_spu_bounds
     */
    @PostMapping("/save")
    public R save(@RequestBody SpuBoundsEntity spuBounds){
		spuBoundsService.save(spuBounds);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuBoundsEntity spuBounds){
		spuBoundsService.updateById(spuBounds);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		spuBoundsService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
