package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    //增加购物车数据
    public void addToCart(String skuId,String userId,Integer skuNum);
    //根据userId查询CartInfo对象 用来购物车列表显示
    public List<CartInfo> getCartList(String userId);
    //合并 cookie到数据库 登录的时候
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);
    //更改选中的属性 ischecked
    public void checkCart(String skuId,String isChecked,String userId);
    //取出redis中那些被选中的数据
    public  List<CartInfo> getCartCheckedList(String userId);
}
