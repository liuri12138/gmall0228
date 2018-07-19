package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

//跳转属性值加载页面
@Controller
public class AttrManageController {

    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;
    @RequestMapping("attrListPage")
    public String attrListPage(){
        return "attrListPage";
    }


//获取一级分类

   @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
       List<BaseCatalog1> catalog1 = manageService.getCatalog1();
       return catalog1;
   }

//获取二级分类
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(@RequestParam Map<String,String> map){
        String catalog1Id = map.get("catalog1Id");
        List<BaseCatalog2> catalog2 = manageService.getCatalog2(catalog1Id);
        return catalog2;
    }

    //获取三级分类
    @RequestMapping("getCatalog3")
    @ResponseBody
    public  List<BaseCatalog3> getCatalog3(@RequestParam Map<String,String>map){
        String catalog2Id = map.get("catalog2Id");
        List<BaseCatalog3> catalog3 = manageService.getCatalog3(catalog2Id);
        return  catalog3;
    }

    //获取属性列表
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(@RequestParam Map<String,String>map){
        String catalog3Id = map.get("catalog3Id");
        List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);
        return attrList;
    }


    //保存数据
    @RequestMapping(value = "saveAttrInfo",method = RequestMethod.POST)
    @ResponseBody
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);

    }
    //编辑数据
    @RequestMapping(value = "getAttrValueList",method = RequestMethod.POST)
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
        BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);
        return attrInfo.getAttrValueList();
    }

    //将skuLSInfo存入es中
    @RequestMapping(value = "onSave",method = RequestMethod.GET)
    @ResponseBody
    public void onSave(String skuId){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        //用beanUtlis将数据考入
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuLsInfo(skuLsInfo);
    }





}