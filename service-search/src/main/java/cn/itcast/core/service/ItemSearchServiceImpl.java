package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        //1. 根据条件, 分页,高亮, 过滤, 排序查询
        Map<String, Object> map = searchMap(searchMap);
        //2. 根据条件分组查询, 根据分类分组, 主要是为了找到查询结果中的分类名称, 分组的目的是为了给分类去重
        List<String> categoryList = findGroupOptions(String.valueOf(searchMap.get("keywords")));
        map.put("categoryList", categoryList);
        //获取传入的分类
        String category = String.valueOf(searchMap.get("category"));
        if (category != null && category.length() > 0) {
            Map bsMap = findBrandListAndSpecListByCategoryName(category);
            map.putAll(bsMap);
        } else {
            Map bsMap = findBrandListAndSpecListByCategoryName(categoryList.get(0));
            map.putAll(bsMap);
        }

        return map;
    }

    private Map<String, Object> searchMap(Map searchMap) {
        Map<String, Object> map = new HashMap<>();
        //1.接受查询条件
        //获取查询关键字
        String keywords = String.valueOf(searchMap.get("keywords"));
        if (keywords != null) {
           keywords = keywords.replaceAll(" ", "");
        }
        //获取当前页
        Integer pageNo = Integer.parseInt(String.valueOf(searchMap.get("pageNo")));
        //获取每页显示数
        Integer pageSize = Integer.parseInt(String.valueOf(searchMap.get("pageSize")));
        //获取用户选中的分类过滤条件
        String category = String.valueOf(searchMap.get("category"));
        //获取用户选中的品牌过滤条件
        String brand = String.valueOf(searchMap.get("brand"));
        //获取用户选中的规格过滤对象
        String spec = String.valueOf(searchMap.get("spec"));
        //获取用户选中的价格区间
        String price = String.valueOf(searchMap.get("price"));
        //获取排序的条件字段 //亦是price
        String sortField = String.valueOf(searchMap.get("sortField"));
        //获取排序的方式
        String sortTy = String.valueOf(searchMap.get("sort"));


        //2.设置查询条件
        //创建查询对象
        HighlightQuery query = new SimpleHighlightQuery();
        //创建条件查询对象
        Criteria contains = new Criteria("item_keywords").contains(keywords);
        // 将查询条件放入查询对象中
        query.addCriteria(contains);
        if (pageNo == null||pageNo<=0) {
            pageNo = 0;
        }
        //设置从第几个开始查询
        query.setOffset((pageNo - 1) * pageSize);
        //设置每页显示多少
        query.setRows(pageSize);
        //设置在标题域中高亮显示
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);

        //查询对象中加入过滤条件
        //按照分类
        if (category != null && category.length() > 0) {
            //创建过滤对象
            FilterQuery simpleFilterQuery = new SimpleFilterQuery();
            //创建过滤条件
            Criteria item_category = new Criteria("item_category").is(category);
            //存入过滤对象
            simpleFilterQuery.addCriteria(item_category);
            //将过滤对象加到查询
            query.addFilterQuery(simpleFilterQuery);
        }
        //按照品牌
        if (brand != null && brand.length() > 0) {
            //创建过滤对象
            FilterQuery filterQuery = new SimpleFilterQuery();
            //创建过滤条件
            Criteria item_brand = new Criteria("item_brand").is(brand);
            //存入
            filterQuery.addCriteria(item_brand);
            //将过滤对象加入查询对象
            query.addFilterQuery(filterQuery);
        }
        //按照规格查询
        if (spec != null && spec.length() > 0) {
            //将页面传入的过滤条件json字符串转换成map
            Map<String, String> mapSpec = JSON.parseObject(spec, Map.class);
            if (mapSpec != null && mapSpec.size() > 0) {
                Set<Map.Entry<String, String>> entries = mapSpec.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                    Criteria item_spec = new Criteria("item_spec_"+entry.getKey()).is(entry.getValue());
                    filterQuery.addCriteria(item_spec);
                    query.addFilterQuery(filterQuery);
                }
            }
        }
        //按照价格区间查询
        if (price != null && price.length() > 0) {
            //获取最大值与最小值得数据
            String[] split = price.split("-");
            if (split != null && split.length == 2) {
                //最小值不等于0,
                if (!"0".equals(split[0])) {
                    SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                    Criteria criteria = new Criteria("item_price").greaterThanEqual(split[0]);
                    filterQuery.addCriteria(criteria);
                    query.addFilterQuery(filterQuery);
                }
                //最大值不等于*
                if (!"*".equals(split[1])) {
                    SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                    Criteria criteria = new Criteria("item_price").lessThanEqual(split[1]);
                    filterQuery.addCriteria(criteria);
                    query.addFilterQuery(filterQuery);
                }
            }

        }
        //价格排序
        if (sortTy != null && sortField != null && !"".equals(sortField) && !"".equals(sortTy)) {
            //升序
            if ("ASC".equals(sortTy)) {
                //创建排序对象, 第一个参数排序方式, 第二个参数:排序的域名
                Sort orders = new Sort(Sort.Direction.ASC, "item_" + sortField);
                //放入查询对象
                query.addSort(orders);
            }
            //降序
            if ("DESC".equals(sortTy)) {
                Sort orders = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(orders);
            }
        }


        //3.查询并返回结果
        HighlightPage<Item> items = solrTemplate.queryForHighlightPage(query, Item.class);
        //获取高亮结果集
        List<HighlightEntry<Item>> highlighted = items.getHighlighted();
        List<Item> itemList = new ArrayList<>();
        for (HighlightEntry<Item> highlightEntry : highlighted) {
            //获取不带高亮的标题
            Item item = highlightEntry.getEntity();
            if (highlightEntry.getHighlights() != null && highlightEntry.getHighlights().size() > 0) {
                List<String> snipplets = highlightEntry.getHighlights().get(0).getSnipplets();
                if (snipplets != null && snipplets.size() > 0) {
                    //获取到高亮标题
                    String s = snipplets.get(0);
                    //如果获得替换原本的标题
                    item.setTitle(s);
                }
            }
            itemList.add(item);
        }
        //封装返回的查询到的结果集
        map.put("rows", itemList);
        //总条数
        map.put("total", items.getTotalElements());
        //总页数
        map.put("totalPages", items.getTotalPages());
        return map;
    }

    private List<String> findGroupOptions(String key){
        List<String> list = new ArrayList<>();
        //获取查询个关键字
        String keywords = String.valueOf(key);
        if (keywords != null) {
            keywords = keywords.replaceAll(" ", "");
        }
        //创建查询对象
        SimpleQuery query = new SimpleQuery();
        //创建条件对象
        Criteria item_keywords = new Criteria("item_keywords").is(keywords);
        //将条件对象存入查询对象
        query.addCriteria(item_keywords);

        //创建分组对象
        GroupOptions groupOptions = new GroupOptions();
        //根据那个域分组
        groupOptions.addGroupByField("item_category");
        //将分组对象存入查询对象
        query.setGroupOptions(groupOptions);

        //分组查询并返回结果集
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query,Item.class);
        //获取分组后的结果集
        GroupResult<Item> item_category = groupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        for (GroupEntry<Item> groupEntry : groupEntries) {
            String groupValue = groupEntry.getGroupValue();
            list.add(groupValue);
        }
        return list;
    }

    private Map findBrandListAndSpecListByCategoryName(String category) {
        //通过分类名称获取获取模板id
        Long templateId = (Long) redisTemplate.boundHashOps(Constants.CATEGORY_LIST_REDIS).get(category);
        //通过模板id获取品牌集合
        List<Map> brandList = (List<Map>)redisTemplate.boundHashOps(Constants.BRAND_LIST_REDIS).get(templateId);
        //通过模板id获取规格集合
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps(Constants.SPEC_LIST_REDIS).get(templateId);
        //4. 将品牌集合和规格集合封装到map中返回
        Map hashMap = new HashMap<>();
        hashMap.put("brandList", brandList);
        hashMap.put("specList", specList);
        return hashMap;
    }


}
