package com.atguigu.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.apache.http.impl.execchain.TunnelRefusedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {
    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;
    @RequestMapping("/{skuId}.html")
    //该控制器需要登录
    /*@LoginRequire(autoRedirect = true)*/
    public String skuInfo(@PathVariable(value = "skuId") String skuId, Model model){

        /*根据skuId查询商品信息  以及图片信息*/


        //查询商品信息
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        model.addAttribute("skuInfo",skuInfo);
        
        //根据skuId和spuId查询属性及属性值
        List<SpuSaleAttr> saleAttrList  = manageService.selectSpuSaleAttrListCheckBySku(skuInfo);
        model.addAttribute("saleAttrList",saleAttrList);

        //组装后台传递到前台的json字符串
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu  = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        //声明字符串
        String valueIdsKey="";
        //定义map
        HashMap<String, String> map = new HashMap<>();
        //拼接
        for (int i = 0; i <skuSaleAttrValueListBySpu.size() ; i++) {
            //取第一个值
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            //加|
            if(valueIdsKey.length()>0){
                valueIdsKey+="|";
            }

            valueIdsKey+=skuSaleAttrValue.getSaleAttrValueId();
            //什么时候停止拼接
            if ((i+1)==skuSaleAttrValueListBySpu.size()|| !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())){
                map.put(valueIdsKey,skuSaleAttrValue.getSkuId());
                valueIdsKey="";
            }

        }
        // 将map 转换成json字符串
        String valueJson = JSON.toJSONString(map);

        System.out.println("valueJson:="+valueJson);

        model.addAttribute("valuesSkuJson",valueJson);


        //跟新热度
        listService.incrHotScore(skuId);
        return "item";
    }


}
