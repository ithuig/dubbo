package cn.itcast.core.util;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ImportDataToSolr {

    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemDataToSolr() {
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andStatusEqualTo("1");
        List<Item> items = itemDao.selectByExample(itemQuery);
        if (items != null) {
            for (Item item : items) {
                String spec = item.getSpec();
                Map map = JSON.parseObject(spec);
                item.setSpecMap(map);
            }
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }

}
