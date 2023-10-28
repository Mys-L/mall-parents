package com.mall.cart.service;

import com.mall.cart.vo.Cart;
import com.mall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @ClassName: CartService
 * @Description:
 * @Author: L
 * @Create: 2023-10-27 22:27
 * @Version: 1.0
 */
public interface CartService {
    /**
     * 将商品添加购物车
     * @param skuId
     * @param num
     * @return
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物车中某个购物项的信息 解决重复刷新提交问题
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取整个购物车
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车数据
     */
    void  clearCart(String cartKey);

    /**
     * 勾选购物车中的商品
     * @param skuId
     * @param checked
     */
    void checkItem(Long skuId, Integer checked);
    /**
     * 改变商品数量
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 删除购物车中的商品
     */
    void deleteCartItemById(Integer skuId);
}
