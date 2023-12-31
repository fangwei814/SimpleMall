package com.fangw.simplemall.cart.service;

import java.util.List;

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

    /**
     * 修改购物项数量
     * 
     * @param skuId
     * @param num
     */
    void countItem(Long skuId, Integer num);

    /**
     * 删除某项
     * 
     * @param skuId
     */
    void deleteItem(Long skuId);

    /**
     * 获取所有选中的购物项
     * 
     * @return
     */
    List<CartItem> getCurrentUserCartItems();
}
