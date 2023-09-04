package com.fangw.simplemall.cart.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fangw.common.utils.R;
import com.fangw.simplemall.cart.feign.ProductFeignService;
import com.fangw.simplemall.cart.interceptor.CartInterceptor;
import com.fangw.simplemall.cart.service.CartService;
import com.fangw.simplemall.cart.vo.Cart;
import com.fangw.simplemall.cart.vo.CartItem;
import com.fangw.simplemall.cart.vo.SkuInfoVo;
import com.fangw.simplemall.cart.vo.UserInfoTo;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    private static final String CART_PREFIX = "simplemall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        // 1.判断购物车有无商品
        String res = (String)cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            // 2.没有就添加新商品
            CartItem cartItem = new CartItem();

            // 3.远程查询要添加的商品信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                cartItem.setSkuId(skuId);
                cartItem.setCheck(true);
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setPrice(data.getPrice());
                cartItem.setCount(num);
            }, executor);

            // 4.远程查修你sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);

            // 5.往redis里放数据
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues);
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);

            return cartItem;
        } else {
            // 6.购物车有商品，增加数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String)cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() {
        // 1.判断登录状态
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (Objects.nonNull(userInfoTo.getUserId())) {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();

            // 2.如果使用用户登录并且有临时购物车数据，那么合并
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItem = getCartItems(tempCartKey);
            if (Objects.nonNull(tempCartItem)) {
                // 合并数据
                for (CartItem cartItem : tempCartItem) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
            }

            // 3.删除临时购物车
            clearCart(tempCartKey);

            // 4.获取登录后购物车数据
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        } else {
            // 5.没登陆就直接查询
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();

            // 获取临时购物车的所有项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    private List<CartItem> getCartItems(String cartKey) {
        // 获取购物车里面的所有商品
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if (Objects.nonNull(values) && !values.isEmpty()) {
            List<CartItem> cartItemVoStream = values.stream().map((obj) -> {
                String str = (String)obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return cartItemVoStream;
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        // 直接删除该键
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 获取要操作的购物车
     * 
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        // 1.判断用户是否登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (Objects.nonNull(userInfoTo.getUserId())) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        // 2.绑定hash
        return redisTemplate.boundHashOps(cartKey);
    }
}
