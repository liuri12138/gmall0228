package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;

// 商品检索接口
public interface ListService {

    // 保存商品信息到es
    void saveSkuLsInfo(SkuLsInfo skuLsInfo);
    // 准备完成dsl语句的功能
    SkuLsResult search(SkuLsParams skuLsParams);
    //跟新热度 排序
    public void incrHotScore(String skuId);



}
