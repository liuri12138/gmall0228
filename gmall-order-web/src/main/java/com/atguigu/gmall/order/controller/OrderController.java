package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {
    @Reference
    private UserAddressService userAddressService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;

    /*@RequestMapping("trade")

    public List<UserAddress> trade(HttpServletRequest request){
        String userId = request.getParameter("userId");
        List<UserAddress> userAddressList = userAddressService.getUserAddressList(userId);
        return userAddressList;
    }*/

    //结算页显示
    @RequestMapping("trade")
    @LoginRequire(autoRedirect = true)
    public  String tradeInit(HttpServletRequest request, Model model){
        //从cartList中将被选中的数据取出来
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);
        //收货人地址
        List<UserAddress> userAddressList  = userAddressService.getUserAddressList(userId);
        model.addAttribute("addressList",userAddressList);
        //订单详情  从cartCheckedList中获取
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail =new OrderDetail();
            //赋值
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            //存入集合
            orderDetailList.add(orderDetail);
        }
        model.addAttribute("orderDetailList",orderDetailList);
        //数据展示 orderInfo orderDetail
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        //设置总价

        model.addAttribute("totalAmount",orderInfo.getTotalAmount());
        //数据库插入 点击提交订单的时候 插入数据
        //保存流水号给前台
        String tradeNo = orderService.getTradeNo(userId);
        model.addAttribute("tradeCode",tradeNo);

        return  "trade";
    }

    //点击提交订单时
    @RequestMapping(value = "submitOrder",method = RequestMethod.POST)
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request,Model model){
        //获取uesrId
        String userId = (String) request.getAttribute("userId");
        //防止重复提交
        String tradeNo = request.getParameter("tradeNo");
        //比较redis  防止重复条
        boolean flag = orderService.checkTradeCode(tradeNo, userId);
        if (!flag){
            model.addAttribute("errMsg","提交订单失败，请联系管理员！");
            return "tradeFail";
        }
        //库存验证
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            // 从订单中去购物skuId，数量
            boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!result){
                request.setAttribute("errMsg","商品库存不足，请重新下单！");
                return "tradeFail";
            }
        }
        //初始化参数
        orderInfo.sumTotalAmount();
        //orderInfo.getTotalAmount();
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        orderInfo.setUserId(userId);
        // 删除redis 中的tradeNo
        orderService.delTradeNo(userId);
        //保存
        String orderId =orderService.saveOrder(orderInfo);

        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }



}
