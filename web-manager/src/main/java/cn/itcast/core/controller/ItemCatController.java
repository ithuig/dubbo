package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    ItemCatService itemCatService;

    @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(Long parentId) {
        List<ItemCat> byParentId = itemCatService.findByParentId(parentId);
        System.out.println(byParentId);
        return byParentId;
    }

    /*分类管理分页*/
    @RequestMapping("/search")
    public PageResult search(@RequestBody ItemCat itemCat, Integer page, Integer rows) {
        if (itemCat.getParentId() == null) {
            itemCat.setParentId(0L);
        }

        //System.out.println(itemCat.getParentId());
        PageResult search = itemCatService.search(itemCat, page, rows);
        //System.out.println("search==="+search.getRows());
        return search;
    }
}
