package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    // 根据spuId查询 属性集合！
    List<SpuSaleAttr> selectSpuSaleAttrList(long spuId);
    //根据skuId和spuId查询属性及属性值
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long skuId,long spuId);
}
