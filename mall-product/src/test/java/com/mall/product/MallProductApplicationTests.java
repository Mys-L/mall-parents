package com.mall.product;

import com.mall.product.entity.BrandEntity;
import com.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MallProductApplicationTests {

	@Autowired
	BrandService brandService;


	@Test
	void contextLoads() {
		List<BrandEntity> list = brandService.list();
		list.forEach(brandEntity -> {
			System.out.println(brandEntity.getName());
		});
	}

}
