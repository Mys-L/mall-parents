package com.mall.product;

import com.mall.product.entity.BrandEntity;
import com.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Autowired
	RedissonClient redissonClient;
	@Test
	void contextLoads() {
		List<BrandEntity> list = brandService.list();
		list.forEach(brandEntity -> {
			System.out.println(brandEntity.getName());
		});
	}

	@Test
	void TestRedisson(){
		System.out.println(redissonClient);
	}

}
