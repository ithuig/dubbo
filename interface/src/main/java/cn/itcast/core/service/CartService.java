package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;

import java.util.List;

public interface CartService {
    //添加商品到购物车
    public List<BuyerCart> addGoodsToCartList(List<BuyerCart> cartList, Long itemId, Integer num);

    //将用户名和购物车集合存入redis
    void setCartListToRedis(String name, List<BuyerCart> cartList);

    //根据商家id查询购物车
    BuyerCart findBuyerCartSellerId(String seller, List<BuyerCart> cartList);

    //传入商品数量和库存信息生成购物车明细对象
    OrderItem createOrderItem(Item item, Integer num);

    //判断购物车列表中是否有该商品
    OrderItem findOrderItemByItemId(List<OrderItem> orderItemList, Long itemId);

    //根据用户名获取redis购物车集合
    List<BuyerCart> getCartListFromRedis(String username);

    //合并Cookie购物车集合与redis购物车集合合并
    List<BuyerCart> mergeCookieCartListToRedisCartList(List<BuyerCart> cookieBuyerCart, List<BuyerCart> redisBuyerCartList);
}
