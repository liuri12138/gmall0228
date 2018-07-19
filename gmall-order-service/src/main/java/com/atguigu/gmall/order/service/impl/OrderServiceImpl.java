package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private RedisUtil redisUtil;


    //保存订单信息 并将订单id orderId返回 用于支付宝支付
    @Override
    public String saveOrder(OrderInfo orderInfo) {
        //保存数据
        orderInfo.setCreateTime(new Date());
        //使用日历
        Calendar calendar = Calendar.getInstance();
        //当前日期的前一天
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //设置out_trade_no :这个是支付宝需要的
        String out_trade_no ="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(out_trade_no);
        orderInfoMapper.insert(orderInfo);
        //将orderInfo的id返回
        return orderInfo.getId();
    }
    //防止表单重复提交
    //生成tradeNo流水号
    public String getTradeNo(String userId){
        //生成key
        String tradeNoKey="user:"+userId+":tradeCode";
        //生成流水号 tradeNo
        String tradeNo = UUID.randomUUID().toString();
        //存入redis中
        Jedis jedis = redisUtil.getJedis();
        jedis.setex(tradeNoKey,10*60,tradeNo);
        return tradeNo;
    }

    //比较流水号 check
    public boolean checkTradeCode(String tradeNo,String userId){
        //取出redis中的tradeNo
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode  = jedis.get(tradeNoKey);
        if(tradeCode!=null&&!"".equals(tradeCode)){
            if(tradeCode.equals(tradeNo)){
                return true;
            }else {
                return false;
            }
        }
        return false;
    }

    //删除redis中的流水号  当第一次提交成功后删除
    public void delTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }
    //库存
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return  true;
        }else {
            return  false;
        }


    }
    //根据orderId查询orderInfo信息
    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        return orderInfo;

    }

}
