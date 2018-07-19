package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class cartController {
    @Reference
    private CartService cartService;
    @Autowired
    private CartCookieHandler cartCookieHandler;
    @Reference
    private ManageService manageService;

    //添加购物车
    @RequestMapping(value = "addToCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response,Model model){
        //取出表单中的信息 item
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");
        //判断用户是否登录
        if(userId!=null){
            //说明用户登录  调用service  走数据库 存入redis
            cartService.addToCart(skuId,userId, Integer.parseInt(skuNum));
        }else {
            //没有登录  存入cookie中
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
        //获取skuInfo信息  success 需要
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        model.addAttribute("skuInfo",skuInfo);
        model.addAttribute("skuNum",skuNum);
        return "success";
    }

    //查出CartInfo  如果登录从缓存或数据库中查  没登录从cookie中查
    //将对象返回页面  用来显示 cartlist 展示购物车列表
    @RequestMapping(value = "cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response,Model model){
        //获取userId
        String userId = (String) request.getAttribute("userId");
        // 取得cookie中所有的cartInfo 数据
        List<CartInfo> cartListFromCookie =cartCookieHandler.getCartList(request);
        //获得的CartInfo存入这个集合
        List<CartInfo> cartList=null;
        if(userId!=null){
            if(cartListFromCookie!=null &&cartListFromCookie.size()>0){
                // 合并购物车，cookie-->db。 根据skuId 相同的就合并，合并完之后，返回一个集合
                cartList = cartService.mergeToCartList(cartListFromCookie, userId);
                // cookie删除掉。
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                // 判断用户是否登录，登录了从redis中，redis中没有，从数据库中取
                cartList = cartService.getCartList(userId);
            }
           model.addAttribute("cartList",cartList);
        }else{
            // 没有登录，从cookie中取得
             cartList =cartCookieHandler.getCartList(request);
            model.addAttribute("cartList",cartList);
        }
        return "cartList";
    }

    //更改选中状态
    @RequestMapping(value = "checkCart",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId = (String) request.getAttribute("userId");
        //判断是否登录
        if(userId!=null){
            //说明登录了 需要将数据取出来 操作redis
            cartService.checkCart(skuId,isChecked,userId);

        }else {
            //未登录
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    //结算  cookie+db
    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        //获取用户id
        String userId = (String) request.getAttribute("userId");
        //取出cookie中的数据 与db合并
        List<CartInfo> cookieHandlerCartList  = cartCookieHandler.getCartList(request);
        if(cookieHandlerCartList!=null&&cookieHandlerCartList.size()>0){
            //合并
            List<CartInfo> cartInfoList = cartService.mergeToCartList(cookieHandlerCartList, userId);
            //将cookie中的值删掉
            cartCookieHandler.deleteCartCookie(request,response);

        }


                


        return "redirect://order.gmall.com/trade";
    }
}
