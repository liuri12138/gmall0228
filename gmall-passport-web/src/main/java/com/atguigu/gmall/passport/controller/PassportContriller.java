package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passport.util.JwtUtil;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportContriller {
    @Value("${token.key}")
    private String signKey;
    @Reference
    private UserInfoService userInfoService;

    //页面跳珠
    @RequestMapping("index")
    public String index(HttpServletRequest request){
        //拼接一个你后面的地址 如果为空 则跳转主页
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    //登录
    @RequestMapping(value = "login",method = RequestMethod.POST)
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){
        //获取ip 通过Nginx的反向代理得到的
        //proxy_set_header X-forwarded-for $proxy_add_x_forwarded_for;
        String ip = request.getHeader("X-forwarded-for");
        if(userInfo!=null){
            UserInfo info = userInfoService.login(userInfo);
            if(info!=null){
                //当用户登录成功候 生成token
                //生成token
                HashMap map = new HashMap();
                map.put("userId",info.getId());
                map.put("nickName",info.getNickName());
                String token = JwtUtil.encode(signKey, map, ip);
                System.out.println("token========"+token);
                return token;
            }
        }

        return "fail";
    }

    //验证
    //思路 解密  获取用户id  根据id从Redis获取数据
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        //获取token 和id
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        //解密
        Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        if(map!=null){
            String userId = (String) map.get("userId");
            UserInfo userInfo=userInfoService.verify(userId);
            if(userInfo!=null){
                return "success";
            }
        }
        return "fail";
    }


}
