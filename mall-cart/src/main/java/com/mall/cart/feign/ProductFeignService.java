package com.mall.cart.feign;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @ClassName: ProductFeignService
 * @Description:
 * @Author: L
 * @Create: 2023-10-28 20:32
 * @Version: 1.0
 */
@FeignClient("mall-product")
public interface ProductFeignService {
    /**
     * 根据skuId查询sku信息
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("product/skusaleattrvalue/stringList/{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);
}
