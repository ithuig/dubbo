package com.pinyougou.page.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.CmsService;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
* ServletContextAware是spring中的接口, spring控制这个接口的初始化
 * 这个接口中有servletContext对象, 已经被spring初始化了, 我们实现这个接口, 实现它的方法
 * 有spring初始化ServletContextAware接口中的servletContext对象, 我们用这个对象给我们
 * 本类中的servletContext属性赋值, 也就是servletContext对象被初始化了
 */
@Service
public class CmsServiceImpl implements CmsService,ServletContextAware {
    @Autowired
    GoodsDao goodsDao;

    @Autowired
    GoodsDescDao goodsDescDao;

    @Autowired
    ItemDao itemDao;

    @Autowired
    ItemCatDao itemCatDao;

    ServletContext servletContext;

    @Autowired
    FreeMarkerConfigurer freeMarkerConfigurer;
    @Override
    public void createStaticPage(Map<String, Object> rootMap, Long goodsId) throws Exception {
        //获取模板初始化对象
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        //加载模板对象
        Template template = configuration.getTemplate("item.ftl");
        //定义输出流,指定文件生成的路径和文件名
        String path = goodsId + ".html";
        //由相对路径转化为绝对路径
        String realPath = getRealPath(path);
        //定义输出流  并且要设置字符集编码
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(realPath)), "UTF-8");
        //生成静态页面
        template.process(rootMap, out);
        //关闭流对象
        out.close();
    }

    private String getRealPath(String path) {
        String realPath = servletContext.getRealPath("");
        System.out.println("项目的绝对路径:"+realPath);
        realPath = servletContext.getRealPath(path);
        return realPath;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public Map<String, Object> findGoodDate(Long goodsId) {
        Map<String, Object> map = new HashMap<>();
        //获取商品数据
        Goods goods = goodsDao.selectByPrimaryKey(goodsId);
        //获取商品详情数据
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(goodsId);
        //获取库存数据
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);
        List<Item> items = itemDao.selectByExample(itemQuery);
        //获取商品分类数据
        if (goods != null) {
            ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
            ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
            ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
            //分类名称存入map
            map.put("itemCat1", itemCat1.getName());
            map.put("itemCat2", itemCat2.getName());
            map.put("itemCat3", itemCat3.getName());
        }
        //将数据存入map
        map.put("goods", goods);
        map.put("goodsDesc", goodsDesc);
        map.put("itemList", items);

        return map;
    }
}
