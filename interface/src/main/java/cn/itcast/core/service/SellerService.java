package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.seller.Seller;

public interface SellerService {
    public void add(Seller seller);

    /*分页*/
    public PageResult search(Seller seller, Integer page, Integer rows);

    /*回显*/
    public Seller findOne(String id);

    /*审核修改*/
    public void update(String sellerId, String status);

    /*删除*/
    public void delete(String[] ids);

    /*信息修改*/
    public void updateSeller(Seller seller);

    /*修改密码*/
    public boolean updatePassWordTwo(String name, String password);
}
