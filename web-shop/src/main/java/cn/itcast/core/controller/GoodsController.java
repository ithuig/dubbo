package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    GoodsService goodsService;
//    @Reference
//    SaveManageToSolr saveManageToSolr;

    @RequestMapping("/add")
    public Result add(@RequestBody GoodsEntity goodsEntity) {
        try {
            //获取当前登录用户的用户名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //设置商品的卖家id
            goodsEntity.getGoods().setSellerId(name);
            goodsService.add(goodsEntity);
            return new Result(true, "添加商品成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加商品失败");
        }
    }

    @RequestMapping("/search")
    public PageResult search(@RequestBody Goods goods, Integer page, Integer rows) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(name);
        PageResult search = goodsService.search(goods, page, rows);
        return search;
    }

    //回显
    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id) {
        return goodsService.findOne(id);
    }

    //修改
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsEntity goodsEntity) {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            String sellerId = goodsEntity.getGoods().getSellerId();
            System.out.println(sellerId+"==="+name);
            if (sellerId.equals(name)) {
                goodsService.update(goodsEntity);
                return new Result(true, "修改成功");
            } else {
                return new Result(false, "用户名不一致");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    //删除
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            if (ids != null) {
                for (Long id : ids) {
                    goodsService.delete(id);
                /*    //根据商品id从solr库中删除数据
                    saveManageToSolr.deleteSolr(id);*/
                }
            }
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }
}
