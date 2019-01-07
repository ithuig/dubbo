package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/content")
public class ContentController {
    @Reference
    ContentService contentService;
    /*广告分页查询*/
    @RequestMapping("/search")
    public PageResult search(@RequestBody Content content, Integer page, Integer rows) {
        return contentService.search(content, page, rows);
    }

    /*广告添加*/
    @RequestMapping("/add")
    public Result add(@RequestBody Content content) {
        try {
            contentService.add(content);
            return new Result(true, "添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败!");
        }
    }

    /*广告回显*/
    @RequestMapping("/findOne")
    public Content findOne(Long id) {
        return contentService.findOne(id);
    }

    /*广告修改*/
    @RequestMapping("/update")
    public Result update(@RequestBody Content content) {
        try {
            contentService.update(content);
            return new Result(true, "修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /*广告删除*/
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            contentService.delete(ids);
            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }
}
