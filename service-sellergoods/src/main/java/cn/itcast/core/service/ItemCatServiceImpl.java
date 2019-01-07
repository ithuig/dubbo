package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {
    @Autowired
    ItemCatDao itemCatDao;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<ItemCat> findByParentId(Long parentId) {

        ItemCatQuery itemCatQuery = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<ItemCat> itemCats = itemCatDao.selectByExample(itemCatQuery);
        return itemCats;
    }

    @Override
    public ItemCat findOne(Long id) {
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(id);
        return itemCat;
    }

    @Override
    public List<ItemCat> findAll() {
        List<ItemCat> itemCats = itemCatDao.selectByExample(null);
        return itemCats;
    }

    @Override
    public PageResult search(ItemCat itemCat, Integer page, Integer rows) {

        //查询所有分类数据
        List<ItemCat> items = itemCatDao.selectByExample(null);
        //循环添加将  分类名称为key, 对应的模板id为value, 存入redis中
        for (ItemCat item : items) {
            redisTemplate.boundHashOps(Constants.CATEGORY_LIST_REDIS).put(item.getName(),item.getTypeId());
        }

        PageHelper.startPage(page, rows);
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();
        if (itemCat.getName() != null) {
            criteria.andNameLike("%" + itemCat.getName() + "%");
        }
        criteria.andParentIdEqualTo(itemCat.getParentId());
        List<ItemCat> itemCats = itemCatDao.selectByExample(itemCatQuery);
        //System.out.println("itemCats"+itemCats);
        PageInfo<ItemCat> itemCatPageInfo = new PageInfo<>(itemCats);
        //System.out.println(itemCatPageInfo.getList());
        return new PageResult(itemCatPageInfo.getTotal(),itemCatPageInfo.getList());
    }
}
