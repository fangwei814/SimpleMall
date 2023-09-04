package com.fangw.simplemall.cart.service;

import com.fangw.simplemall.cart.vo.CartItem;

public interface CartService {
    /**
     * 加入商品到购物车
     * 
     * @param skuId
     * @param num
     * @return
     */
    CartItem addToCart(Long skuId, Integer num);

    /**
     * 查询购物车
     * 
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);
}
