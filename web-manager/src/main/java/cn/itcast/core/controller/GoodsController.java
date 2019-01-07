package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.CmsService;
import cn.itcast.core.service.GoodsService;
import cn.itcast.core.service.SaveManageToSolr;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    GoodsService goodsService;

    @Reference
    SaveManageToSolr saveManageToSolr;

    @Reference
    CmsService cmsService;


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

    //删除
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            if (ids != null) {
                for (Long id : ids) {
                    goodsService.delete(id);
                 /*   //根据商品id从solr库中删除数据
                    saveManageToSolr.deleteSolr(id);*/
                }
            }

            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    //修改
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            //修改数据库信息
            if (ids != null) {
                for (Long id : ids) {
                    //1. 更改数据库中商品的审核状态
                    goodsService.updateStatus(id,status);
            /*        //2. 判断商品审核状态是否为1, 审核通过
                    if ("1".equals(status)) {
                        //3. 根据商品id, 获取商品详细数据, 放入solr索引库中供前台系统搜索使用
                        saveManageToSolr.saveItemToSolr(id);
                        //4. 根据商品id, 获取商品详细数据, 通过数据和模板生成商品详情页面
                        Map<String, Object> goodDate = cmsService.findGoodDate(id);
                        cmsService.createStaticPage(goodDate,id);
                    }*/
                }
            }
            return new Result(true,"审批完成");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "失败");
        }
    }
}
