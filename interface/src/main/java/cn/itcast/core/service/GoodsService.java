package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import org.springframework.web.bind.annotation.RequestBody;

public interface GoodsService {

    //添加
    public void add(GoodsEntity goodsEntity);

    //分页
    public PageResult search(@RequestBody Goods goods, Integer page, Integer rows);

    //回显
    public GoodsEntity findOne(Long id);

    //修改
    public void update(GoodsEntity goodsEntity);

    public void delete(Long id);

    //审核审批
    public void updateStatus(Long id, String status);
}
