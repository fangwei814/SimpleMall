package com.fangw.simplemall.cart.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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
