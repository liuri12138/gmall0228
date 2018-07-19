package com.atguigu.gmall.service;



import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {
    //查询所有
    public List<UserInfo> allUserInfo();

    //模糊查询
    public List<UserInfo> getLikeUserInfo();

    //添加 全部
    public  void  addUserInfo(UserInfo userInfo);
    // 添加 选择添加
    public void add(UserInfo userInfo);

    //修改
    void updataUserInfo(UserInfo userInfo);

    //根据注册账号修改
    void updateByName(UserInfo userInfo);

    //删除
    void deleteUserInfo(UserInfo userInfo);
    void  del(UserInfo userInfo);

    //登录
    UserInfo login(UserInfo userInfo);
    //验证
    UserInfo verify(String userId);

}
