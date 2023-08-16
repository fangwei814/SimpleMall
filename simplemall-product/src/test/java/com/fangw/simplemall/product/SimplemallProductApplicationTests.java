package com.fangw.simplemall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fangw.simplemall.product.entity.BrandEntity;
import com.fangw.simplemall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SimplemallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Test
	void contextLoads() {
//		BrandEntity brandEntity = new BrandEntity();
//
//		brandEntity.setDescript("");
//		brandEntity.setName("huawei");
//		brandService.save(brandEntity);
//
//		System.out.println("Insert succeed.");

		List<BrandEntity> brandId = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
		brandId.forEach(brand -> {
			System.out.println(brand);
		});
	}

}
