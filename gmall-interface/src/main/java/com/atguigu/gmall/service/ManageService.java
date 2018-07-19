package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManageService {
    public List<BaseCatalog1> getCatalog1();
    public List<BaseCatalog2> getCatalog2(String catalog1Id);
    public List<BaseCatalog3> getCatalog3(String catalog2Id);
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    //保存数据
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    //编辑
    BaseAttrInfo getAttrInfo(String attrId);

    // 查询spu列表信息
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);
    //获取基本属性
    List<BaseSaleAttr> getBaseSaleAttrList();
    //保存数据spu
    public void saveSpuInfo(SpuInfo spuInfo);
    //回显销售属性
    public List<SpuSaleAttr> getSpuSaleAttrList( String spuId);
    //回显图片
    public List<SpuImage> getSpuImageList(String spuId);
    //保存所有
    public void  saveSku(SkuInfo skuInfo);
    //根据skuId查询商品信息
    public SkuInfo getSkuInfo(String skuInfo);
    //根据skuId和spuId查询属性及属性值
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);
    // 根据spuId 拼接属性值
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
    //根据属性Id查询属性及值
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
