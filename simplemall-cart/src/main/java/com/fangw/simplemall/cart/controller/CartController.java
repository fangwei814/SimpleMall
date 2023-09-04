package com.fangw.simplemall.cart.controller;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fangw.simplemall.cart.interceptor.CartInterceptor;
import com.fangw.simplemall.cart.service.CartService;
import com.fangw.simplemall.cart.vo.CartItem;
import com.fangw.simplemall.cart.vo.UserInfoTo;

@Controller
public class CartController {
    @Autowired
    CartService cartService;

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
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes ra)
        throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.addToCart(skuId, num);
        ra.addAttribute("skuId", skuId);
        return "redirect:http://cart.simplemall.com/addToCartSuccess.html";
    }

    /**
     * 跳转到成功页
     * 
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        // 重定向到成功页面，再次查询购物车数据
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }
}
