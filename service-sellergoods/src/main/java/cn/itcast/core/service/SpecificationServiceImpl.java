package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    SpecificationDao specificationDao;
    @Autowired
    SpecificationOptionDao specificationOptionDao;
    @Override
    public PageResult search(Specification specification, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
            }
        }
        List<Specification> specifications = specificationDao.selectByExample(specificationQuery);
        PageInfo pageInfo = new PageInfo(specifications);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /*添加*/
    public void add(SpecEntity specEntity) {
        //保存规格对象
        specificationDao.insertSelective(specEntity.getSpecification());
        //保存规格选项集合
        if (specEntity.getSpecificationOptionList() != null) {
            for (SpecificationOption specificationOption : specEntity.getSpecificationOptionList()) {
                //设置规格外键
                specificationOption.setSpecId(specEntity.getSpecification().getId());
                //保存规格
                specificationOptionDao.insertSelective(specificationOption);
            }
        }
    }

    /*修改*/
    @Override
    public void update(SpecEntity specEntity) {
/*        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        criteria.andIdEqualTo(specEntity.getSpecification().getId());*/
        //修改规格根据id
        specificationDao.updateByPrimaryKeySelective(specEntity.getSpecification());
        //删除全部规格选项
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(specEntity.getSpecification().getId());
        specificationOptionDao.deleteByExample(specificationOptionQuery);
        //遍历添加规格选项
        if (specEntity.getSpecificationOptionList() != null) {
            for (SpecificationOption specificationOption : specEntity.getSpecificationOptionList()) {
                //设置规格外键
                specificationOption.setSpecId(specEntity.getSpecification().getId());
                specificationOptionDao.insertSelective(specificationOption);
            }

        }

    }

    /*回显*/
    @Override
    public SpecEntity findOne(Long id) {
        //创建SpecEntity类
        SpecEntity specEntity = new SpecEntity();
        //获取规格对象并保存
        Specification specification = specificationDao.selectByPrimaryKey(id);
        specEntity.setSpecification(specification);
        //通过where查询获取规格选项对象
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(specificationOptionQuery);
        //并保存
        specEntity.setSpecificationOptionList(specificationOptions);
        return specEntity;
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                //删除指定id的全部规格选项
                SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
                criteria.andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(specificationOptionQuery);
                //删除指定id的规格
                specificationDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }


}
