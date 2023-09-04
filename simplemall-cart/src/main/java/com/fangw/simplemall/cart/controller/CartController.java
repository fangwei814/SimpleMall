package com.fangw.simplemall.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.fangw.simplemall.cart.interceptor.CartInterceptor;
import com.fangw.simplemall.cart.vo.UserInfoTo;

@Controller
public class CartController {
    @GetMapping("/cart.html")
    public String cartListPage() {
        // 1.从threadlocal中获取登录信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        return "cartList";
    }

    /**
     * 添加商品到购物车
     * 
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart() {
        return "success";
    }
}
