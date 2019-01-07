package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.entity.PageResult;

import java.util.List;

public interface ContentService {

    /*广告分页查询*/
    public PageResult search(Content content, Integer page, Integer rows);

    /*广告添加*/
    public void add(Content content);

    /*广告回显*/
    public Content findOne(Long id);

    /*广告修改*/
    public void update(Content content);

    /*广告删除*/
    public void delete(Long[] ids);

    public List<Content> findByCategoryId(Long categoryId);
}
