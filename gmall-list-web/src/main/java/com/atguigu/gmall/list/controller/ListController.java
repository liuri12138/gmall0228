package com.atguigu.gmall.list.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {
    @Reference
    private ListService listService;
    @Reference
    private ManageService manageService;
    //查询页面数据
    @RequestMapping(value = "list.html",method = RequestMethod.GET)
    public String getSkuLsResult(SkuLsParams skuLsParams,Model model){
        //设置每页的条数
        skuLsParams.setPageSize(2);
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        System.out.println(JSON.toJSONString(skuLsResult));

        //将数据返回页面
        model.addAttribute("skuLsInfoList",skuLsResult.getSkuLsInfoList());
        //差属性 及值 并返回
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        //根据查出来的属性值id查询baseAttrValue 并显示在页面上
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);

        model.addAttribute("attrList",attrList);

        //  做个url拼接 ,参数skuLsParams.
        String makeUrl = makeUrlParam(skuLsParams);
        // 已选的属性值列表
        List<BaseAttrValue> baseAttrValuesList = new ArrayList<>();
        // makeUrl 是针对于属性id而来，而SkuLsParams 也有可能会携带跟属性id相同的查询条件。【如果有相同，则应该去掉相同部分条件】
        //  itco 在集合遍历期间应该使用 迭代器，不能使用for循环
        //attrList 是从结果集中根据valueId查出来显示在页面上的
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            //取得每一个BaseAttrInfo中的对象
            BaseAttrInfo baseAttrInfo =  iterator.next();
            //取得每一个属性值
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            //循环
            for (BaseAttrValue baseAttrValue : attrValueList) {
                if (baseAttrValue.getId()!=null &&baseAttrValue.getId().length()>0){
                    //判断属性值id和skuLsParams中的id集合是否相同
                    //skuLsParams.getValueId是参数中携带的
                    if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                        for (String valueId : skuLsParams.getValueId()) {
                            if(valueId.equals(baseAttrValue.getId())){
                                iterator.remove();
                                //构造面包屑列表
                                BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                                //将属性值名拼接为 “属性名：属性值名”
                                baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                                // 去除重复数据
                                String makeUrlParam  = makeUrlParam(skuLsParams, valueId);
                                baseAttrValueSelected.setUrlParam(makeUrlParam);
                                baseAttrValuesList.add(baseAttrValueSelected);

                            }
                        }
                    }
                }
            }
        }
        //设置总页数 需要设置显示的页数 每页的条数 和总页数
        int totalPages = (int) ((skuLsResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize());
        model.addAttribute("totalPages",totalPages);
        model.addAttribute("pageNo",skuLsParams.getPageNo());



        //将从新制作的url保存
        model.addAttribute("urlParam",makeUrl);
        //将选中的baseAttrValueSelected的集合放入model中

        model.addAttribute("baseAttrValuesList",baseAttrValuesList);
        model.addAttribute("keyword",   skuLsParams.getKeyword());

        return "list";
    }
    //拼接字符串的方法  并且判断传递过来的参数是否存在于makeUrl中
    private String makeUrlParam(SkuLsParams skuLsParams,String...excludeValueIds) {
        String makeUrl="";
        //拼接关键字
        if(skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            makeUrl+="keyworf="+skuLsParams.getKeyword();
        }
        //三级分类拼接
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            if(makeUrl.length()>0){
                makeUrl+="&";
            }
            makeUrl+="catalog3Id="+skuLsParams.getCatalog3Id();
        }
        //属性值id 的拼接
        if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            for (int i = 0; i <skuLsParams.getValueId().length; i++) {
                //b遍历获取每一个属性值id
                String valueId =skuLsParams.getValueId()[i];
                //取得传递过来的参数 并与makeUrl中的比较
                if(excludeValueIds!=null && excludeValueIds.length>0){
                    String excludeValueId=excludeValueIds[0];
                    if(excludeValueId.equals(valueId)){
                        //如果相同 则停止本次循环 进入下一次循环
                        continue;
                    }
                }
                if(makeUrl.length()>0){
                    makeUrl+="&";
                }
                makeUrl+="valueId="+valueId;
            }
        }
        return makeUrl;
    }
}
