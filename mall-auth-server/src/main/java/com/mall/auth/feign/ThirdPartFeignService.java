package com.mall.auth.feign;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName: ThirdPartFeignService
 * @Description:
 * @Author: L
 * @Create: 2023-10-16 14:14
 * @Version: 1.0
 */
@FeignClient("mall-third-party")
public interface ThirdPartFeignService {
    @GetMapping("/sms/sendcode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
