package com.fangw.simplemall.search.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.fangw.simplemall.search.service.MallSearchService;
import com.fangw.simplemall.search.vo.SearchParam;
import com.fangw.simplemall.search.vo.SearchResult;

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    /**
     * 页面跳转
     *
     * @param param
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model) {
        SearchResult result = mallSearchService.search(param);

        model.addAttribute("result", result);

        return "list";
    }
}
