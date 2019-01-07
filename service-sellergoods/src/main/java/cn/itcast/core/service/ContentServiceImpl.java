package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {
    @Autowired
    ContentDao contentDao;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public PageResult search(Content content, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        ContentQuery contentQuery = new ContentQuery();
        ContentQuery.Criteria criteria = contentQuery.createCriteria();
        if (content.getTitle() != null && content.getTitle().length() > 0) {
            criteria.andTitleLike("%" + content.getTitle() + "%");
        }
        List<Content> contents = contentDao.selectByExample(contentQuery);
        PageInfo<Content> contentPageInfo = new PageInfo<>(contents);
        return new PageResult(contentPageInfo.getTotal(), contentPageInfo.getList());
    }

    /*广告添加*/
    @Override
    public void add(Content content) {
        contentDao.insertSelective(content);
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
    }

    /*回显*/
    @Override
    public Content findOne(Long id) {
        return contentDao.selectByPrimaryKey(id);
    }

    /*修改*/
    @Override
    public void update(Content content) {
        //这里穿入得Content是要修改的类对象
        //首先查询先查询redis数据库.mysql里面是没有改变的先要删除mysql数据库里的数据
        // 通过传入的广告对象的id获取mysql数据库里的广告对象(id是不变的无法修改的)
        Content oldContent = contentDao.selectByPrimaryKey(content.getId());
        //通过获取到的广告对象的分类id删除数据
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(oldContent.getCategoryId());
        //根据从网页获取到的广告对象的分类id删除的redis数据库,便于修改之后重新查询
        redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                //根据id去数据库查询广告对象
                Content content = contentDao.selectByPrimaryKey(id);
                // 根据广告对象中的分类id, 删除redis中对应的广告集合数据
                redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).delete(content.getCategoryId());
                //从数据库删除对应的数据
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    /*根据分类广告id查询*/
    public List<Content> findByCategoryId(Long categoryId) {
        //查询redis
        List<Content> contents = (List<Content>) redisTemplate.boundHashOps(Constants.CONTENT_LIST_REDIS).get(categoryId);
        //判断数据是否为空
        if (contents == null) {
            //如果为空查询mysql
            ContentQuery contentQuery = new ContentQuery();
            ContentQuery.Criteria criteria = contentQuery.createCriteria();
            if (categoryId != null) {
                criteria.andCategoryIdEqualTo(categoryId);
            }
            criteria.andStatusEqualTo("1");
            contentQuery.setOrderByClause("sort_order");
            contents = contentDao.selectByExample(contentQuery);
            redisTemplate.boundHashOps("contents").put(categoryId, contents);
        }

        return contents;
    }
}
