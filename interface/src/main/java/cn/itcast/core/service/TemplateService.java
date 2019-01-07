package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;
import org.apache.velocity.Template;

import java.util.List;
import java.util.Map;

public interface TemplateService {
    public PageResult findPage(TypeTemplate template, Integer page, Integer rows);

    public TypeTemplate findOne(Long id);

    public void add(TypeTemplate template);

    public void update(TypeTemplate template);

    public void delete(Long[] ids);

    public List<Map> findBySpecList(Long id);

}
