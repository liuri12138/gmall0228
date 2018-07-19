package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Reference
    private ManageService manageService;
    @Autowired
    private RedisUtil redisUtil;
    //添加购物车
    @Override
    public void addToCart(String skuId,String userId,Integer skuNum) {
        //查询cart中是否有这件商品
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist  = cartInfoMapper.selectOne(cartInfo);
        if(cartInfoExist!=null){
            //说明购物车中有这件商品  需要更新数量
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else {
            //说明没有该商品  需要添加 其中的skuInfo需要manageservice
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 =new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            // 插入数据库
             cartInfoMapper.insertSelective(cartInfo1);
             //将这个对象给cartInfoExist
            cartInfoExist = cartInfo1;
        }
        // 构建key user:userid:info
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //存入缓存中
        Jedis jedis = redisUtil.getJedis();
        //将对象序列化
        String cartJson  = JSON.toJSONString(cartInfoExist);
        jedis.hset(userCartKey,skuId,cartJson);
        // 更新购物车过期时间
        String userInfoKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
    }


    //根据userId查询购物车  显示在页面 cartList
    @Override
    public List<CartInfo> getCartList(String userId) {
        //先看缓存  再看数据库 定义redis的key
        String userCartKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        //从redis中取出数据
        List<String> cartJsons  = jedis.hvals(userCartKey);
        if(cartJsons!=null && !"".equals(cartJsons)){
            //用来存对象
            List<CartInfo> cartInfoList = new ArrayList<>();
            for (String  cartJson  : cartJsons) {
                //转换成对象
                CartInfo cartInfo = JSON.parseObject(cartJson , CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            //排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;
        }else {
            // 从数据库中查询，其中cart_price 可能是旧值，所以需要关联sku_info 表信息。
            List<CartInfo> cartInfoList = loadCartCache(userId);
            return  cartInfoList;
        }
    }
    //合并cookie到数据库 登录时
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
        //先取出数据库中的数据
        List<CartInfo> cartInfoListDB  = cartInfoMapper.selectCartListWithCarPrice(userId);
        //判断循环
        for (CartInfo cartInfoCK  : cartListFromCookie) {
            // 有相同的，没有相同[insert]
            boolean isMatch = false;
            for (CartInfo infoDB  : cartInfoListDB) {
                //如果skuId相同则说明数据库有这个数据 需要更新数量
                if(cartInfoCK.getSkuId().equals(infoDB.getSkuId())){
                    infoDB.setSkuNum(infoDB.getSkuNum()+cartInfoCK.getSkuNum());
                    //更新数据库
                    cartInfoMapper.updateByPrimaryKeySelective(infoDB);
                    isMatch=true;
                }
            }
            //插入数据
            if(!isMatch){
                //说明数据库中没有这个数据
                // userId 赋值
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        // loadCartCache ： 根据userId 先查数据库，在放缓存
        List<CartInfo> cartInfoList = loadCartCache(userId);
        //因为在结算合并时数据的ischecked没有更改（cookie中如果有选中的），所以要根据skuid查出来再改
        for (CartInfo cartInfoDB : cartInfoList) {
            for (CartInfo infoCK : cartListFromCookie) {
                if(cartInfoDB.getSkuId().equals(infoCK.getSkuId())){
                    if("1".equals(infoCK.getIsChecked())){
                        cartInfoDB.setIsChecked("1");
                        //放入缓存中
                        checkCart(cartInfoDB.getSkuId(),"1",userId);
                    }
                }
            }
        }
        return cartInfoList;
    }

    //更改商品选中状态  ischecked
    @Override
    public void checkCart(String skuId,String isChecked,String userId) {
        //从redis中根据skuid选出商品
        String userCartKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        //因为是根据 skuId所以只能取出一个
        String cartJson=jedis.hget(userCartKey,skuId);
        //将取出来的字符串转换为对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        //将修改后的数据存入redis
        String cartCheckdJson  = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,skuId,cartCheckdJson);

        // 新增到已选中购物车 将所有选中的商品再保存到redis中
        String userCheckedKey =CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        if(isChecked.equals("1")){
            //如果为1  则存入redis
            jedis.hset(userCheckedKey,skuId,cartCheckdJson);
        }else {
            //如果不为1 则删除
            jedis.hdel(userCheckedKey,skuId);
        }
    }
    
    //取出redis中被选中的数据
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        String userCheckedKey =CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedList  = jedis.hvals(userCheckedKey);
        //将这个字符串遍历
        //存入对象的集合
        List<CartInfo> newCartList = new ArrayList<>();
        for (String cartJson  : cartCheckedList) {
            //将每一个遍历的字符串转化成对象存入集合
            CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
            newCartList.add(cartInfo);
        }
        return newCartList;
    }

    //sku_info 和cartInfo 关联查询 查出数据库中的cartInfo对象  将sku_info的数据给cartInfo
    private List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList=cartInfoMapper.selectCartListWithCarPrice(userId);
        //判断
        if(cartInfoList!=null && cartInfoList.size()>0){
            //准备放入redis
            Jedis jedis = redisUtil.getJedis();
            //定义key
            // 对数据进行转换 hset(key,field,value);
            String userCartKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
            // field，value === 正好对应上我们的map.put(field,value) jedis.hmset(userCartKey,map);
            Map<String,String> map = new HashMap<>(cartInfoList.size());
            //遍历
            for (CartInfo cartInfo : cartInfoList) {
                //将cartInfo转为字符串
                String cartJson   = JSON.toJSONString(cartInfo);
                //field=skuId value=cartJson
                map.put(cartInfo.getSkuId(),cartJson);
            }
            //存入redis
            jedis.hmset(userCartKey,map);
            jedis.close();

        }
        return cartInfoList;
    }
}
