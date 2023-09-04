package com.fangw.simplemall.cart.service;

import com.fangw.simplemall.cart.vo.Cart;
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

    /**
     * 获取购物车所有信息
     * 
     * @return
     */
    Cart getCart();

    /**
     * 清空购物车
     * 
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 选中某一项
     * 
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);
}
