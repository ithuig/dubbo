package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.*;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsDao goodsDao;
    @Autowired
    GoodsDescDao goodsDescDao;
    @Autowired
    ItemDao itemDao;
    @Autowired
    SellerDao sellerDao;
    @Autowired
    ItemCatDao itemCatDao;
    @Autowired
    BrandDao brandDao;

    @Autowired
    JmsTemplate jmsTemplate;
    //用于商品上架
    @Autowired
    ActiveMQTopic topicPageAndSolrDestination;
    //用于商品下架
    @Autowired
    ActiveMQQueue queueSolrDeleteDestination;
    @Override
    public void add(GoodsEntity goodsEntity) {
        //1.保存商品表的数据
        //设置初始化新增商品状态为0,未审核
        goodsEntity.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(goodsEntity.getGoods());

        //2.保存商品详情表的数据
        goodsEntity.getGoodsDesc().setGoodsId(goodsEntity.getGoods().getId());
        goodsDescDao.insertSelective(goodsEntity.getGoodsDesc());

        //3.保存库存集合数据
        insertItem(goodsEntity);
    }

    //分页
    @Override
    public PageResult search(Goods goods, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        if (goods != null) {
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0 && !"tom".equals(goods.getSellerId())) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
        }
        List<Goods> goods1 = goodsDao.selectByExample(goodsQuery);
        PageInfo<Goods> goodsPageInfo = new PageInfo<>(goods1);
        return new PageResult(goodsPageInfo.getTotal(),goodsPageInfo.getList());
    }

    //回显
    @Override
    public GoodsEntity findOne(Long id) {
        //根据商品id查询商品对象
        Goods goods = goodsDao.selectByPrimaryKey(id);
        //创建GoodsEntity实体类对象
        GoodsEntity goodsEntity = new GoodsEntity();
        //根据商品id查询商品详情对象
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        //查询库存集合对象
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(itemQuery);
        //4.将上面查询到的所有数据封装到GoodsEntity实体当中
        goodsEntity.setGoods(goods);
        goodsEntity.setGoodsDesc(goodsDesc);
        goodsEntity.setItemList(items);
        return goodsEntity;
    }

    //修改
    @Override
    public void update(GoodsEntity goodsEntity) {
        //修改商品信息
        goodsDao.updateByPrimaryKeySelective(goodsEntity.getGoods());
        //修改商品详情信息
        goodsDescDao.updateByPrimaryKeySelective(goodsEntity.getGoodsDesc());
        //修改规格集合数据
        //先根据id删除全部的信息
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(goodsEntity.getGoods().getId());
        itemDao.deleteByExample(itemQuery);
        //添加
        insertItem(goodsEntity);
    }

    //删除
    @Override
    public void delete(final Long id) {
         Goods goods = new Goods();
         goods.setId(id);
         goods.setIsDelete("1");
         goodsDao.updateByPrimaryKeySelective(goods);
         //将下架的商品id作为消息发送给消息服务器
        // 发送消息,第一个参数是发送到的队列,用于消息接收方接受判断
        // 第二个参数是接口,定义发送的内容
        jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                return textMessage;
            }
        });
    }
    //审核审批
    @Override
    public void updateStatus(final Long id, String status) {
         //修改商品状态
         Goods goods = new Goods();
         goods.setAuditStatus(status);
         goods.setId(id);
         goodsDao.updateByPrimaryKeySelective(goods);

         //修改库存状态
        Item item = new Item();
        item.setStatus(status);
        //where
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        itemDao.updateByExampleSelective(item, itemQuery);

        //判断如果审核通过, 将商品id作为消息发送给消息服务器
        if ("1".equals(status)) {
            //发送消息,第一个参数是发送到的队列,第二个参数是接口,定义发送的内容
            jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                    return textMessage;
                }
            });
        }

    }


    //插入库存数据
    public void insertItem(GoodsEntity goodsEntity) {
        //"1"是启用规格
        if ("1".equals(goodsEntity.getGoods().getIsEnableSpec())) {
            List<Item> itemList = goodsEntity.getItemList();
            //判断库存集合是否为空
            if (itemList != null && itemList.size() > 0) {
                //遍历库存集合,保存库存对象
                for (Item item : itemList) {
                    //库存数据标题,商品名称+ 具体规格组成的库存标题,目的是为了消费者搜索的时候搜索的更精确
                    String goodsName = goodsEntity.getGoods().getGoodsName();
                    //获取库存对象中的规格json字符串
                    String spec = item.getSpec();
                    //将json数组转换成一个实体类对象
                    Map maps = JSON.parseObject(spec, Map.class);
                    if (maps != null) {
                        Collection<String> values = maps.values();
                        for (String value : values) {
                            goodsName += " " + value;
                        }
                    }
                    item.setTitle(goodsName);
                  //设置Item对象属性值
                    setItemValue(goodsEntity,item);
                    //存入数据库
                    itemDao.insertSelective(item);
                }
            }

        } else {
            //如果网页没有勾选规格,初始化一条数据库
            Item item = new Item();
            //设置库存标题
            item.setTitle(goodsEntity.getGoods().getGoodsName());
            //设置规格
            item.setSpec("{}");
            //设置默认库存量
            item.setNum(0);
            //设置默认价格
            item.setPrice(new BigDecimal("9999999"));
            //设置item对象
            setItemValue(goodsEntity, item);
            itemDao.insertSelective(item);
        }

    }

    // 设置item库存对象属性值
    public void setItemValue(GoodsEntity goodsEntity, Item item) {
        //设置item对象的属性值
        //库存状态默认是未审核"0"
        item.setStatus("0");
        //设置对应商品的id
        item.setGoodsId(goodsEntity.getGoods().getId());
        //创建时间
        item.setCreateTime(new Date());
        //修改时间
        item.setUpdateTime(new Date());
        //卖家名称
        Seller seller = sellerDao.selectByPrimaryKey(goodsEntity.getGoods().getSellerId());
        item.setSeller(seller.getName());
        //分类id ,使用商品中的第三级分类作为这里的分类id和分类名称
        item.setCategoryid(goodsEntity.getGoods().getCategory3Id());
        //分类名称
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(goodsEntity.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
        // 品牌名称
        Brand brand = brandDao.selectByPrimaryKey(goodsEntity.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //商品示例图片
        String itemImages = goodsEntity.getGoodsDesc().getItemImages();
        if (itemImages != null) {
            List<Map> maps1 = JSON.parseArray(itemImages, Map.class);
            if (maps1 != null && maps1.size() > 0) {
                String url = String.valueOf(maps1.get(0).get("url"));
                item.setImage(url);
            }
        }
    }

}
