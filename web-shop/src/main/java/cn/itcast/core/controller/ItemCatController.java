package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    ItemCatService itemCatService;

    //五级联动
    @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(Long parentId) {
        List<ItemCat> byParentId = itemCatService.findByParentId(parentId);
        return byParentId;
    }

    @RequestMapping("/findOne")
    public ItemCat findOne(Long id) {
        ItemCat itemCat = itemCatService.findOne(id);
        return itemCat;
    }

    @RequestMapping("/findAll")
    public List<ItemCat> findAll() {
            List<ItemCat> items = itemCatService.findAll();
        System.out.println(items);

            return items;
    }
}
