package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SpuManageController {
    @Reference
    private ManageService manageService;

    @RequestMapping("spuListPage")
    public String spuListPage(){
        return "spuListPage";
    }



//查询属性列表属性
    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> getSpuList(String catalog3Id){
        // 创建spuInfo对象
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = manageService.getSpuInfoList(spuInfo);
        return spuInfoList;
    }

    //获取基本属性
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = manageService.getBaseSaleAttrList();
        return  baseSaleAttrList;
    }

    //保存数据
    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    @ResponseBody
    private void saveSpuInfo(SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);

    }

    //获取sku中的销售属性回想
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId){
        return manageService.getSpuSaleAttrList(spuId);
    }
    //回显图片
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){
        return manageService.getSpuImageList(spuId);
    }

}
