package com.fangw.simplemall.product.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fangw.simplemall.product.service.SkuInfoService;
import com.fangw.simplemall.product.vo.SkuItemVo;

@Controller
public class ItemController {
    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     * 
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);

        return "item.html";
    }
}
