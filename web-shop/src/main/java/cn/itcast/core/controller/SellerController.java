package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    SellerService sellerService;

    @RequestMapping("/add")
    public Result add(@RequestBody Seller seller) {
        try {
            sellerService.add(seller);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

    @RequestMapping("/findOne")
    public Seller findOne(String id) {
        Seller one = sellerService.findOne(id);
        return one;
    }

    @RequestMapping("/find")
    public Seller find() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Seller seller = sellerService.findOne(name);
        return seller;
    }

    @RequestMapping("/update")
    public Result update(@RequestBody Seller seller) {
        try {
            sellerService.updateSeller(seller);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    //修改密码
    @RequestMapping("/updatePassWordTwo")
    public Result updatePassWordTwo(String password) {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            boolean b = sellerService.updatePassWordTwo(name, password);
            if (b) {
                return new Result(true, "修改成功");
            } else {
                return new Result(false, "密码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "未知异常");
        }
    }
}
