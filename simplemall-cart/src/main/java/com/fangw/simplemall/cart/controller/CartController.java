package com.fangw.simplemall.cart.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fangw.simplemall.cart.service.CartService;
import com.fangw.simplemall.cart.vo.Cart;
import com.fangw.simplemall.cart.vo.CartItem;

@Controller
public class CartController {
    @Autowired
    CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems() {
        return cartService.getCurrentUserCartItems();
    }

    /**
     * 删除某项
     * 
     * @param skuId
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.simplemall.com/cart.html";
    }

    /**
     * 修改某项数量
     * 
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItem(skuId, num);
        return "redirect:http://cart.simplemall.com/cart.html";
    }

    /**
     * 选中取消某项
     * 
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.simplemall.com/cart.html";
    }

    /**
     * 购物车页面
     * 
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);

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
