package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {
    @Reference
    private ManageService manageService;


    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    // 未登录的时候，添加到购物车
   public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum){
        //从cookie中获取 看是否存在
       String cartJson  = CookieUtil.getCookieValue(request, cookieCartName, true);
        //存cookie中的cartInfo
       List<CartInfo> cartInfoList = new ArrayList<>();
       boolean ifExist=false;
       if(cartJson!=null){
           //将cartJson转换为对象  可能有多个商品 需要转为对象的集合
           cartInfoList  = JSON.parseArray(cartJson, CartInfo.class);
           for (CartInfo cartInfo : cartInfoList) {
              //判断cookie中的商品是否和要放入的一样
               if(cartInfo.getSkuId().equals(skuId)){
                   //将数量更新
                   cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                   //更新价格
                   cartInfo.setSkuPrice(cartInfo.getCartPrice());
                   ifExist=true;
               }
           }
       }
       //说明没有相同的商品
       if(!ifExist){
           //根据skuId将商品查出来  存入cartInfo中
           SkuInfo skuInfo = manageService.getSkuInfo(skuId);
           CartInfo cartInfo =new CartInfo();
           //存数据
           cartInfo.setSkuId(skuId);
           cartInfo.setCartPrice(skuInfo.getPrice());
           cartInfo.setSkuPrice(skuInfo.getPrice());
           cartInfo.setSkuName(skuInfo.getSkuName());
           cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

           cartInfo.setUserId(userId);
           cartInfo.setSkuNum(skuNum);
           //将商品放集合中
           cartInfoList.add(cartInfo);
       }
       //将集合转为字符创
       String newCartJson  = JSON.toJSONString(cartInfoList);
       //存到cookie中
       CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);

   }

   //从cookie中获取cartInfo
    public List<CartInfo> getCartList(HttpServletRequest request) {
        String cartJson  = CookieUtil.getCookieValue(request, cookieCartName, true);
        //将这个串转换为对象
        List<CartInfo> cartInfoList  = JSON.parseArray(cartJson, CartInfo.class);
        return cartInfoList;
    }
    //清空cookie
    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
       CookieUtil.deleteCookie(request,response,cookieCartName);
    }
    //更改被选中的状态
    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
       //将cookie中的所有值
        List<CartInfo> cartList = getCartList(request);
        for (CartInfo cartInfo : cartList) {
            //根据skuid 将相同的取出来  更改状态
            if(cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
            //改完后再存进去
            CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartList),COOKIE_CART_MAXAGE,true);
        }
    }
}
