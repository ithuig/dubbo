package cn.itcast.core.service;


import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;

import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TemplateServiceImpl implements TemplateService {
    @Autowired
    TypeTemplateDao typeTemplateDao;

    @Autowired
    SpecificationOptionDao specificationOptionDao;

    @Autowired
    RedisTemplate redisTemplate;

    /*分页查询*/
    @Override
    public PageResult findPage(TypeTemplate template, Integer page, Integer rows) {
        //获取所有模板数据
        List<TypeTemplate> typeTemplatesFall = typeTemplateDao.selectByExample(null);
        for (TypeTemplate typeTemplate : typeTemplatesFall) {
            //模板id作为key,对应的品牌集合作为values存入redis数据库
           List<Map> map = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
           //存入
            redisTemplate.boundHashOps(Constants.BRAND_LIST_REDIS).put(typeTemplate.getId(), map);
            //模板id作为key,对应的规格集合作为values存入redis数据库
            List<Map> specList = findBySpecList(typeTemplate.getId());
            redisTemplate.boundHashOps(Constants.SPEC_LIST_REDIS).put(typeTemplate.getId(),specList);
        }
        PageHelper.startPage(page, rows);
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = typeTemplateQuery.createCriteria();
        if (template != null) {
            if (template.getName() != null && template.getName().length() > 0) {
                criteria.andNameLike("%" + template.getName() + "%");
            }
        }
        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(typeTemplateQuery);
        PageInfo pageInfo = new PageInfo(typeTemplates);
//        Page<TypeTemplate> pageInfo = (Page<TypeTemplate>) typeTemplateDao.selectByExample(null);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /*修改回显*/
    @Override
    public TypeTemplate findOne(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        return typeTemplate;
    }

    /*添加*/
    @Override
    public void add(TypeTemplate template) {
        typeTemplateDao.insertSelective(template);
    }

    /*修改*/
    @Override
    public void update(TypeTemplate template) {
        typeTemplateDao.updateByPrimaryKeySelective(template);
    }

    /*删除*/
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateDao.deleteByPrimaryKey(id);
        }
    }

    /*获取规格属性*/
    @Override
    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        List<Map> maps = JSON.parseArray(typeTemplate.getSpecIds(),Map.class);
        if (maps!=null) {
            for (Map map : maps) {
            /*SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
            SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
            criteria.andSpecIdEqualTo(Long.valueOf(String.valueOf(map.get("id"))));
            List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(specificationOptionQuery);
            map.put("option", specificationOptions);*/
                //5. 在遍历的过程中将根据规格id获取对应的规格选项集合, 并且封装到规格对象中
                Long specId = Long.parseLong(String.valueOf(map.get("id")));

                SpecificationOptionQuery query = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = query.createCriteria();
                criteria.andSpecIdEqualTo(specId);
                //根据规格id查询到规格选项集合
                List<SpecificationOption> optionList = specificationOptionDao.selectByExample(query);
                map.put("options", optionList);
            }
        }
        return maps;
    }


}
