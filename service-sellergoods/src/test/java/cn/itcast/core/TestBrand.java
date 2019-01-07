package cn.itcast.core;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext*.xml"})
public class TestBrand {

    @Autowired
    BrandDao brandDao;
    @Test
    public void tst() {
        List<Brand> brandList = brandDao.selectByExample(null);
        System.out.println(brandList);
    }

    @Test
    public void findById() {
        Brand brand = brandDao.selectByPrimaryKey(1L);
        System.out.println(brand);

    }

    @Test
    public void insert() {
        Brand brand = new Brand();
        int insert = brandDao.insert(brand);
    }
}
