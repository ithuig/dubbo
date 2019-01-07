package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.entity.Result;

import cn.itcast.core.service.CartService;
import cn.itcast.core.util.Constants;
import cn.itcast.core.util.CookieUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/*e     r
添加商品到购物车
     * 要求springMvc4.2以上版本可以支持cors跨域请求解决方法
     * 加上CrossOrigin注解, origins属性写上service_page项目的IP和端口就可以解决跨域请求
*/

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    //商品详情页传来的商品信息   库存id 与 商品数量, 添加到购物车
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:8092", allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        try {
            //1. 获取当前登录用户名称
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //2. 获取购物车列表
            List<BuyerCart> cartList = findCartList();
            //3. 将当前商品加入到购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //4. 判断当前用户是否登录, 未登录用户名为"anonymousUser"
            if ("anonymousUser".equals(name)) {
                //4.a.如果未登录, 则将购物车列表存入cookie中
                CookieUtil.setCookie(request, response, Constants.CART_LIST_COOKIE, JSON.toJSONString(cartList), 60 * 60 * 24 * 30, "utf-8");
            } else {
                //4.b.如果已登录, 则将购物车列表存入redis中
                cartService.setCartListToRedis(name, cartList);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }

    }

    //查询购物车所有列表数据, 返回数据到购物车列表页面

    @RequestMapping("/findCartList")
    public List<BuyerCart> findCartList() {
        //获取登录用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //2. 从cookie中获取购物车列表json格式字符串
        String cookieValue = CookieUtil.getCookieValue(request, Constants.CART_LIST_COOKIE, "utf-8");
        //3. 如果json格式字符串是否为空 , 为空则返回}"[]"
        if (cookieValue == null) {
            cookieValue = "[]";
        }
        //4. 将购物车json对象转化为对象
        List<BuyerCart> cookieBuyerCartList = JSON.parseArray(cookieValue,BuyerCart.class);
        //5. 判断用户是否登录 , 未登录用户是否为"anonymousUser"
        if ("anonymousUser".equals(name)) {
            //5.1 未登录 , 则返回cookie中的购物车对象
            return cookieBuyerCartList;
        } else {
            //5.2 已登录则返回redis中的购物车对象
            List<BuyerCart> redisBuyerCartList= cartService.getCartListFromRedis(name);
            //5.2.1 判断cookie中是否存在购物车数据
            if (cookieBuyerCartList != null && cookieBuyerCartList.size() > 0) {
                //如果cookie中存在 , 则把cookie中的数据存到redis里面去
                redisBuyerCartList = cartService.mergeCookieCartListToRedisCartList(cookieBuyerCartList, redisBuyerCartList);
                //删除cookie中的数据
                CookieUtil.deleteCookie(request, response, Constants.CART_LIST_COOKIE);
                //将合并后的数据存到redis里面去
                cartService.setCartListToRedis(name, redisBuyerCartList);
            }
            //5.3返回购物车对象
            return redisBuyerCartList;
        }
    }
}
