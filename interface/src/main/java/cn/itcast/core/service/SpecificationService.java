package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.specification.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    public PageResult search(Specification specification, Integer page, Integer rows);

    /*添加*/
    public void add(SpecEntity specEntity);

    /*修改*/
    public void update(SpecEntity specEntity);

    /*回显*/
    public SpecEntity findOne(Long id);

    /*删除*/
    public void delete(Long[] ids);

    List<Map> selectOptionList();
}
