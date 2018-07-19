package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    // 根据三级分类id查询属性表
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(Long catalog3Id);
    //根据属性Id集合 查询属性及值
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param(value = "valueIds") String valueIds);
}
