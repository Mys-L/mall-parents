package com.mall.product.feign;

import com.mall.common.to.SkuReductionTo;
import com.mall.common.to.SpuBoundTo;
import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ClassName: CouponFeignService
 * @Description: ：远程调用优惠券服务
 * @Author: L
 * @Create: 2023-07-26 13:49
 * @Version: 1.0
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {

    /**
     *  @RequestBody 将对象转换为json 将上一步的json放在请求体位置 发送请求
     *  对方服务收到请求。 收到的是请求体里的json数据  那边用 @RequestBody 对SpuBoundsEntity进行封装
     *  只要 JSON 数据模型是兼容的 双方服务无需使用同一个 TO 对象
     *  SpuInfoServiceImpl 方法：saveSpuInfo-> 5 保存spu的积分信息 跨数据库 mall_sms -> sms_spu_bounds
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    /**
     *  SpuInfoServiceImpl 方法：saveSpuInfo-> 6.4) 保存sku 的优惠满减等信息 跨数据库保存mall_sms 数据库->sms_sku_ladder(打折) sms_sku_full_reduction(满减) sms_member_price(会员价格)
     */
    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(SkuReductionTo skuReductionTo);
}
