package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface OrderService {
    //保存订单信息 并将订单id orderId返回 用于支付宝支付
    public  String  saveOrder(OrderInfo orderInfo);
    // 生成流水号
    String getTradeNo(String userId);
    // 检查流水号
    boolean checkTradeCode(String tradeNo,String userId);
    // 删除流水号
    void delTradeNo(String userId);
    //库存 查看库存
    boolean checkStock(String skuId, Integer skuNum);
    //根据orderId查询orderInfo信息
    OrderInfo getOrderInfo(String orderId);
}
