package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.controller.ManageConst;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.config.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private  SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private  SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private  SkuImageMapper skuImageMapper;
    @Autowired
    private RedisUtil redisUtil;



    //获取一级目录
    @Override
    public List<BaseCatalog1> getCatalog1() {
        List<BaseCatalog1> baseCatalog1s = baseCatalog1Mapper.selectAll();
        return baseCatalog1s;
    }
    //获取二级目录
    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }
    //获取三级目录
    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3s = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3s;
    }
    //获取属性
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
       /* BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.select(baseAttrInfo);*/
        List<BaseAttrInfo> baseAttrInfoListByCatalog3Id = baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
        return baseAttrInfoListByCatalog3Id;
    }
    //保存方法
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 说明value_name的值没有拿到！
        // 保存数据：编辑数据放到一起来处理。
        // 是否有主键,操作都是指的是平台属性操作
        if(baseAttrInfo.getId()!=null && baseAttrInfo.getId().length()>0){
            //说明为修改属性数据 有id
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else{
            // 没有主键则需要添加,注意一下，当没有主键的时候，数据的id要设置成null，如果不设置有可能会出现空字符串
            if(baseAttrInfo.getId().length()==0){
                baseAttrInfo.setId(null);
            }
            //插入数据
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        // 操作属性值，先将属性值清空
        BaseAttrValue baseAttrValue=new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);
        //开始操作属性值列表
        if(baseAttrInfo.getAttrValueList()!=null && baseAttrInfo.getAttrValueList().size()>0){
            for (BaseAttrValue attrValue :baseAttrInfo.getAttrValueList() ) {
                // 做插入操作
                if(attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }

    }

    // 获取所有的平台属性值！
    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        // attrId 实际上是BaseAttrInfo 的id。
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 创建BaseAttrValue对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        // 将attrId赋值，根据attrId进行查询BaseAttrValue对象
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        // 查询BaseAttrValue的集合
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        // 因为控制器需要的是AttrValueList，所以讲AttrValueList()赋值
        baseAttrInfo.setAttrValueList(attrValueList);
        // 将BaseAttrInfo对象返回
        return baseAttrInfo;
    }

    //
    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        return  spuInfoMapper.select(spuInfo);
    }

    //获取基本销售属性
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectAll();
        return baseSaleAttrs;
    }

    //保存销售属性及图片、属性名 商品表
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
    //保存
        if(spuInfo.getId()!=null && spuInfo.getId().length()>0){
            spuInfoMapper.updateByPrimaryKey(spuInfo);
        }else{
            //判断key
            if(spuInfo.getId()!=null && spuInfo.getId().length()==0){
                spuInfo.setId(null);
            }
            spuInfoMapper.insertSelective(spuInfo);
        }
    //图片保存  先删除再保存
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage image : spuImageList) {
            //判断主键
            if(spuImage.getId()!=null && spuImage.getId().length()==0){
                spuImage.setId(null);
            }
            //设置spuId
            image.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(image);
        }
    //保存属性 和属性值
        //先删除
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);


        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        //保存属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr saleAttr : spuSaleAttrList) {
            //判断
            if(saleAttr.getId()!=null && saleAttr.getId().length()==0){
                saleAttr.setId(null);
            }
            //传入spuId
            saleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(saleAttr);


            //插入属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                if(saleAttrValue.getId()!=null && saleAttrValue.getId().length()==0){
                    saleAttrValue.setId(null);
                }
                saleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }


    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);

        return spuImageMapper.select(spuImage);
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        //添加SkuInfo数据
        if (skuInfo.getId()==null || skuInfo.getId().length()==0){
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        } else {
            skuInfoMapper.updateByPrimaryKey(skuInfo);
        }

        // 先删除，再添加
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        // SkuId = SkuInfo.id
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue attrValue : skuAttrValueList) {
            // 坑！
            attrValue.setSkuId(skuInfo.getId());
            if (attrValue.getId()!=null&& attrValue.getId().length()==0){
                attrValue.setId(null);
            }
            skuAttrValueMapper.insertSelective(attrValue);
        }
        // 属性值添加
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
            saleAttrValue.setSkuId(skuInfo.getId());
            if (saleAttrValue.getId()!=null && saleAttrValue.getId().length()==0){
                saleAttrValue.setSkuId(null);
            }
            skuSaleAttrValueMapper.insertSelective(saleAttrValue);
        }
        // 图片添加
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage image : skuImageList) {
            image.setSkuId(skuInfo.getId());
            if (image.getId()!=null && image.getId().length()==0){
                image.setId(null);
            }
            skuImageMapper.insertSelective(image);
        }

    }
    //查询skuInfo 将内容回显页面
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo=null;
        Jedis jedis = redisUtil.getJedis();
        String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
        //判断是否存在这个key然后根据不同情况完成
        if(jedis.exists(skuInfoKey)){
            //存在 从缓存中取出
            String skuInfoJson  = jedis.get(skuInfoKey);
            if(skuInfoJson!=null || !"".equals(skuInfoJson)){
                //将字符串转化为对象
                skuInfo=  JSON.parseObject(skuInfoJson,SkuInfo.class);
                return skuInfo;

            }
        }else {
            skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
            SkuImage skuImage = new SkuImage();
            skuImage.setSkuId(skuId);
            List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
            skuInfo.setSkuImageList(skuImageList);

            // 添加查询平台属性
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> select = skuAttrValueMapper.select(skuAttrValue);
            skuInfo.setSkuAttrValueList(select);
            //添加销售属性
            SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
            skuSaleAttrValue.setSkuId(skuId);
            skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueMapper.select(skuSaleAttrValue));


            //将skuInfo转换为字符串 存入Redis中
            jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));
            return skuInfo;
        }
        return skuInfo;
    }
    //根据skuId和spuId查询属性及属性值 回想页面
    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        long spuId = Long.parseLong(skuInfo.getSpuId());
        long skuId = Long.parseLong(skuInfo.getId());
        List<SpuSaleAttr> spuSaleAttrs  = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrs ;
    }
    //根据spuId查询对应所有的sku 用来组合生成json类型的 返回页面
    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuSaleAttrValues;
    }
    //根据属性值id查询所有的属性及值
    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        //用StingUtils分割集合
        String valueId = StringUtils.join(attrValueIdList, ",");
        List<BaseAttrInfo> baseAttrInfoList=baseAttrInfoMapper.selectAttrInfoListByIds(valueId);
        return baseAttrInfoList;
    }


}
