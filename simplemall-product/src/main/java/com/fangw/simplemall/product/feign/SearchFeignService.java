package com.fangw.simplemall.product.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fangw.common.es.SkuEsModel;
import com.fangw.common.utils.R;

@FeignClient("simplemall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R ProductStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
