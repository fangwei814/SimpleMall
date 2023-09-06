package com.fangw.simplemall.search.service;

import com.fangw.simplemall.search.vo.SearchParam;
import com.fangw.simplemall.search.vo.SearchResult;

public interface MallSearchService {
    /**
     * 检索商品信息
     * 
     * @param param
     * @return
     */
    SearchResult search(SearchParam param);
}
