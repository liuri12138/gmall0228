package com.atguigu.gmall.usermanager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserAddressService;
import com.atguigu.gmall.usermanager.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;
@Service
public class UserAddressServiceImpl implements UserAddressService{
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);
        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);
        return userAddressList;
    }
}
