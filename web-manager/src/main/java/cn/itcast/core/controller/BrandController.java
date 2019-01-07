package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    BrandService brandService;

    @RequestMapping("/findAll")
    public List<Brand> findAll() {
        List<Brand> brandList = brandService.findAll();
        System.out.println(brandList);
        return brandList;
    }

    @RequestMapping("/search")
    public PageResult search(@RequestBody Brand brand,Integer page, Integer rows) {
        System.out.println(brand);
        PageResult result = brandService.findPage(brand,page, rows);
        return result;
    }

    /*添加*/
    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand) {
        System.out.println(brand);
        try {
            brandService.add(brand);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }

    }

    /*根据id查寻*/
    @RequestMapping("/findById")
    public Brand findById(Long id) {
        Brand brand = brandService.findById(id);
        return brand;
    }

    /*修改*/
    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand) {

        try {
            brandService.update(brand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /*批量删除*/
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            brandService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        List<Map> brandList = brandService.selectOptionList();
        return brandList;
    }

}
