package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ItemCatService {

    public List<ItemCat> findByParentId(Long parentId);

    public ItemCat findOne(Long id);
    public List<ItemCat> findAll();

    public PageResult search(ItemCat itemCat, Integer page, Integer rows);
}
