package com.mall.cart.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.mall.cart.feign.ProductFeignService;
import com.mall.cart.interceptor.CartInterceptor;
import com.mall.cart.service.CartService;
import com.mall.cart.vo.Cart;
import com.mall.cart.vo.CartItem;
import com.mall.cart.vo.SkuInfoVo;
import com.mall.cart.vo.UserInfoTo;
import com.mall.common.constant.Constant;
import com.mall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName: CartServiceImpl
 * @Description:
 * @Author: L
 * @Create: 2023-10-27 22:27
 * @Version: 1.0
 */

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor executor;


    /**
     * 添加购物车的功能
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //判断Redis是否有该商品的信息
        String redisResult = (String) cartOps.get(skuId.toString());
        //如果没有就添加数据
        if (StringUtils.isEmpty(redisResult)) {
            //新商品添加到购物车
            CartItem cartItem = new CartItem();
            //使用多线程进行查询
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                //1 远程调用商品服务查询商品信息
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(1);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setPrice(data.getPrice());
                cartItem.setSkuId(skuId);
            }, executor);
            CompletableFuture<Void> getSkuSaleAttrValuesTask= CompletableFuture.runAsync(() -> {
                //2 远程调用sku的组合信息
                List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(attrValues);
            }, executor);
            //等待所有的异步任务全部完成
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).get();
            String  cartItemJson= JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), cartItemJson);
            return cartItem;
        }else {
            //购物车有此商品，修改数量即可
            CartItem cartItem = JSON.parseObject(redisResult, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            //修改redis的数据
            String cartItemJson = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),cartItemJson);
            return cartItem;
        }

    }
    /**
     * 获取到我们要操作的购物车
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //先得到当前用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = Constant.CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = Constant.CART_PREFIX + userInfoTo.getUserKey();
        }
        //绑定指定的key操作Redis
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }


    @Override
    public CartItem getCartItem(Long skuId) {
        //拿到要操作的购物车信息
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String redisValue = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(redisValue, CartItem.class);
        return cartItem;
    }

    /**
     * 获取购物车 分为两种 用户登录或者未登录购物车里所有的数据
     * @return
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            //1登录
            String cartKey = Constant.CART_PREFIX + userInfoTo.getUserId();
            //2、如果临时购物车的数据还未进行合并
            String tempCartKey = Constant.CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null) {
                //临时购物车有数据 需要合并
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(),tempCartItem.getCount());
                }
                //清除临时购物车的数据
                clearCart(tempCartKey);
            }
            //3、获取登录后的购物车数据【包含合并过来的临时购物车的数据和登录后购物车的数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else {
            //没有登录
            String cartKey = Constant.CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    /**
     * 用户登录或者未登录购物车里所有的数据 -> 获取购物车里面的数据
     * @param cartKey
     */
    private List<CartItem> getCartItems(String cartKey) {
        //获取购物车里面的所有商品
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if (values != null && values.size() > 0) {
            List<CartItem> cartItemList = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).toList();
            return cartItemList;
        }
        return null;
    }

    /**
     * 清空购物车数据
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /**
     * 修改选中的商品
     */
    @Override
    public void checkItem(Long skuId, Integer checked) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(checked==1?true:false);
        String str = JSON.toJSONString(cartItem);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), str);
    }
    /**
     * 修改商品数量
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    /**
     * 删除购物车中的商品
     */
    @Override
    public void deleteCartItemById(Integer skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

}
