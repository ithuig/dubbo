package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.entity.PageResult;

import java.util.List;


public interface ContentCategoryService {
    //分页查询
    public PageResult search(ContentCategory contentCategory, Integer page, Integer rows);

    //添加
    public void add(ContentCategory contentCategory);

    //修改
    public void update(ContentCategory contentCategory);

    //删除
    public void delete(Long[] ids);

    //根据id查询
    public ContentCategory findOne(Long id);

    //查询全部
    public List<ContentCategory> findAll();
}
