package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    @Autowired
    SellerDao sellerDao;

    @Override
    public void add(Seller seller) {
        seller.setCreateTime(new Date());
        seller.setStatus("0");
        sellerDao.insertSelective(seller);
    }

    /*分页与条件查询*/
    @Override
    public PageResult search(Seller seller, Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        SellerQuery sellerQuery = new SellerQuery();
        SellerQuery.Criteria criteria = sellerQuery.createCriteria();
        if (seller != null) {
            if (seller.getStatus() != null && !"".equals(seller.getStatus())) {
                criteria.andStatusEqualTo(seller.getStatus());
            }
            if (seller.getName() != null && seller.getName().length() > 0) {
                criteria.andNameLike("%" + seller.getName() + "%");
            }
            if (seller.getNickName() != null && seller.getNickName().length() > 0) {
                criteria.andNickNameLike("%" + seller.getNickName() + "%");
            }
        }
        List<Seller> sellers = sellerDao.selectByExample(sellerQuery);
        PageInfo pageInfo = new PageInfo(sellers);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /*回显单个Seller对象*/
    @Override
    public Seller findOne(String id) {
        Seller seller = sellerDao.selectByPrimaryKey(id);
        return seller;
    }

    /*修改*/
    @Override
    public void update(String sellerId, String status) {

        Seller seller = new Seller();
        seller.setStatus(status);
        seller.setSellerId(sellerId);
        sellerDao.updateByPrimaryKeySelective(seller);
    }

    /*删除*/
    @Override
    public void delete(String[] ids) {
        for (String id : ids) {
            sellerDao.deleteByPrimaryKey(id);
        }
    }

    /*修改信息*/
    @Override
    public void updateSeller(Seller seller) {
        sellerDao.updateByPrimaryKeySelective(seller);
    }

    //修改密码
    @Override
    public boolean updatePassWordTwo(String name, String password) {
        Seller seller = sellerDao.selectByPrimaryKey(name);
        if (password.equals(seller.getPassword())) {
            seller.setPassword(password);
            sellerDao.updateByPrimaryKeySelective(seller);
            return true;
        } else {
            return false;
        }

    }


}
