package com.atguigu.gmall.usermanager.controller;

import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;

    //查询所有
    @RequestMapping("getAll")
    @ResponseBody
    public List<UserInfo> getAll(){
        List<UserInfo> list = userInfoService.allUserInfo();
        for (UserInfo userInfo : list) {
            System.out.println(list);
        }
        return list;
    }

    @RequestMapping("like")
    @ResponseBody
    public List<UserInfo> like(){
        List<UserInfo> list = userInfoService.getLikeUserInfo();
        for (UserInfo userInfo : list) {
            System.out.println(list);
        }
        return list;
    }

    //插入全部
    @RequestMapping("add")
    @ResponseBody
    public void add(){
        UserInfo userInfo = new UserInfo();
        userInfo.setLoginName("goudan");
        userInfo.setName("狗蛋");
        userInfo.setPasswd("321");
        userInfo.setEmail("lalal");
        userInfoService.addUserInfo(userInfo);
    }
    @RequestMapping("addtwo")
    @ResponseBody
    public void addtwo(){
        UserInfo userInfo = new UserInfo();
        userInfo.setLoginName("caihua");
        userInfo.setName("菜花");
        userInfo.setPasswd("321");
        userInfo.setEmail("lalal");
        userInfoService.add(userInfo);
    }


    @RequestMapping("upd")
    @ResponseBody
    public void upd(UserInfo userInfo){
       userInfo.setId("5");
       userInfo.setLoginName("dongyang");
       userInfoService.updataUserInfo(userInfo);

    }

    //根据LoginName修改字段
    @RequestMapping("upd1")
    @ResponseBody
    public void upd1(UserInfo userInfo){
        userInfo.setLoginName("goudan");
        userInfo.setPhoneNum("1100");
        userInfoService.updateByName(userInfo);

    }

   //删除
    @RequestMapping("delete")
    @ResponseBody
    public void deleteUserInfo(UserInfo userInfo){
        userInfo.setId("5");
        userInfoService.deleteUserInfo(userInfo);
    }

    @RequestMapping("del")
    @ResponseBody
    public void del(UserInfo userInfo){
        userInfo.setId("6");
        userInfoService.del(userInfo);
    }
}