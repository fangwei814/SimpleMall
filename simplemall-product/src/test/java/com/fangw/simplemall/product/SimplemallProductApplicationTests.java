package com.fangw.simplemall.product;

import com.fangw.simplemall.product.entity.BrandEntity;
import com.fangw.simplemall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SimplemallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Test
	void contextLoads() {
		BrandEntity brandEntity = new BrandEntity();

		brandEntity.setDescript("");
		brandEntity.setName("huawei");
		brandService.save(brandEntity);

		System.out.println("Insert succeed.");
	}

}
