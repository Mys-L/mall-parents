package com.mall.product.feign;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 调用远程服务 WareFeignService
 * Description：
 * @author L
 */
@FeignClient("mall-ware")
public interface WareFeignService {

	/**
	 * 修改系统的 R 带上泛型
	 */
	@PostMapping("/ware/waresku/hasStock")
	R getSkuHasStock(@RequestBody List<Long> SkuIds);
}
