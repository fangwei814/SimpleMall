package com.fangw.simplemall.product.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fangw.simplemall.product.entity.CategoryEntity;
import com.fangw.simplemall.product.service.CategoryService;
import com.fangw.simplemall.product.vo.Catalog2Vo;

/**
 * 首页分类
 */
@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 用来渲染一级分类
     * 
     * @param model
     * @return
     */
    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // TODO 1、查找所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        // 视图解析器进行拼串
        // 前缀："classpath:/templates/" 后缀：".html"
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    /**
     * 渲染二级、三级分类
     * 
     * @return
     */
    @ResponseBody
    @GetMapping({"/index/catalog.json"})
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJsonFromDbUsingCache();
        return catalogJson;
    }
}
