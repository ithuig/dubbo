package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;

import java.util.List;
import java.util.Map;

@Service
public class SaveManageToSolrImpl implements SaveManageToSolr {
        @Autowired
        ItemDao itemDao;
        @Autowired
        SolrTemplate solrTemplate;
        @Override
        public void saveItemToSolr(Long id) {
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        //查询指定的商品id的库存数据
        criteria.andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(itemQuery);
        if (items != null) {
            for (Item item : items) {
                String spec = item.getSpec();
                Map map = JSON.parseObject(spec,Map.class);
                item.setSpecMap(map);
            }
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }

    @Override
    public void deleteSolr(Long id) {
        //创建查询对象
        SimpleQuery query = new SimpleQuery();
        //创建条件对象
        Criteria criteria = new Criteria("item_goodsid").is(id);
        //将条件对象放入查询对象
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
