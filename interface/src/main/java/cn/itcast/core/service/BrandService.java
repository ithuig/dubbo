package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    /*查询所有*/
    public List<Brand> findAll();

    /*分页*/
    public PageResult findPage(Brand brand,Integer page, Integer rows);

    /*添加*/
    public void add(Brand brand);

    /*根据id回显*/
    public Brand findById(Long id);

    /*修改*/
    public void update(Brand brand);

    public void delete(Long[] ids);

    /*模板中的brand下拉框查询*/
    public List<Map> selectOptionList();
}
