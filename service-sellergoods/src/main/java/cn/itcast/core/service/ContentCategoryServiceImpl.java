package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentCategoryDao;
import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.ad.ContentCategoryQuery;
import cn.itcast.core.pojo.entity.PageResult;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {
    @Autowired
    ContentCategoryDao contentCategoryDao;
    @Override
    //广告分类分页
    public PageResult search(ContentCategory contentCategory, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        ContentCategoryQuery contentCategoryQuery = new ContentCategoryQuery();
        ContentCategoryQuery.Criteria criteria = contentCategoryQuery.createCriteria();
        if (contentCategory.getName() != null && contentCategory.getName().length() > 0) {
            criteria.andNameLike("%" + contentCategory.getName() + "%");
        }
        List<ContentCategory> contentCategories = contentCategoryDao.selectByExample(contentCategoryQuery);
        PageInfo<ContentCategory> contentCategoryPageInfo = new PageInfo<>(contentCategories);
        return new PageResult(contentCategoryPageInfo.getTotal(),contentCategoryPageInfo.getList());
    }

    //广告分类添加
    @Override
    public void add(ContentCategory contentCategory) {
        contentCategoryDao.insertSelective(contentCategory);
    }

    //广告分类修改
    @Override
    public void update(ContentCategory contentCategory) {
        contentCategoryDao.updateByPrimaryKeySelective(contentCategory);
    }


    //广告分类删除
    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                contentCategoryDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public ContentCategory findOne(Long id) {
        return contentCategoryDao.selectByPrimaryKey(id);
    }

    //查询全部
    @Override
    public List<ContentCategory> findAll() {
        return contentCategoryDao.selectByExample(null);
    }

}
