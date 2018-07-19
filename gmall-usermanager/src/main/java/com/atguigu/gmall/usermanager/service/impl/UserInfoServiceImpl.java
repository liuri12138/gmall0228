package com.atguigu.gmall.usermanager.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.usermanager.mapper.UserInfoMapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisUtil redisUtil;
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60;
    @Override
    public List<UserInfo> allUserInfo() {
        List<UserInfo> userInfos = userInfoMapper.selectAll();
        return userInfos;
    }

    //模糊查询
    @Override
    public List<UserInfo> getLikeUserInfo() {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andLike("loginName","%a%");
        List<UserInfo> userInfoList = userInfoMapper.selectByExample(example);
        return userInfoList;
    }

    @Override
    public void addUserInfo(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    @Override
    public void add(UserInfo userInfo) {
        userInfoMapper.insertSelective(userInfo);
    }

    @Override
    public void updataUserInfo(UserInfo userInfo) {
        userInfoMapper.updateByPrimaryKeySelective(userInfo);

    }

    @Override
    public void updateByName(UserInfo userInfo) {
        //Example example = new Example(UserInfo.class);
        //example.createCriteria().andEqualTo("loginName",userInfo.getLoginName());
        //userInfoMapper.updateByExampleSelective(userInfo,example);

        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("loginName",userInfo.getLoginName());
        userInfoMapper.updateByExampleSelective(userInfo,example);
    }

    //删除
    @Override
    public void deleteUserInfo(UserInfo userInfo) {
        userInfoMapper.deleteByPrimaryKey(userInfo);

    }

    @Override
    public void del(UserInfo userInfo) {
        userInfoMapper.deleteByPrimaryKey(userInfo);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        //给密码解密
        String passwd = userInfo.getPasswd();
        String newPsaaword = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPsaaword);
        UserInfo info = userInfoMapper.selectOne(userInfo);
        if(info!=null){
            // 获得到redis ,将用户存储到redis中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+info.getId()+userinfoKey_suffix,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
        return null;
    }

    //验证
    @Override
    public UserInfo verify(String userId) {
        //从Redis中获取数据
        //定义key
        String key = userKey_prefix+userId+userinfoKey_suffix;
        Jedis jedis = redisUtil.getJedis();
        //判断key是否存在
        if(jedis.exists(key)){
            //重置时间
            jedis.expire(key,userKey_timeOut);
            //获取值
            String userJson = jedis.get(key);
            if(userJson!=null && !"".equals(userJson)){
                //将userJson转换为userInfo对象
                UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
                return userInfo;
            }
        }

        return null;
    }


}
