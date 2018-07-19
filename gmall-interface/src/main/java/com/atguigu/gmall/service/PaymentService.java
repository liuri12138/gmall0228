package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {
    //保存信息
    void savePaymentInfo(PaymentInfo paymentInfo);
    // 修改方法
    void updatePaymentInfo(PaymentInfo paymentInfo,String out_trade_no);
}
