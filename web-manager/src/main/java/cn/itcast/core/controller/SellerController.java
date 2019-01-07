package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    SellerService sellerService;

    /*分页*/
    @RequestMapping("/search")
    public PageResult search(@RequestBody Seller seller, Integer page, Integer rows) {
        System.out.println(seller);
        PageResult search = sellerService.search(seller, page, rows);
        return search;
    }

    /*回显详情*/
    @RequestMapping("/findOne")
    public Seller findOne(String id) {
        Seller seller = sellerService.findOne(id);
        return seller;
    }

    /*修改*/
    @RequestMapping("/updateStatus")
    public Result update(String sellerId, String status) {
        try {
            sellerService.update(sellerId, status);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(String[] ids) {
        try {
            sellerService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }
}
