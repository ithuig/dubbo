package cn.itcast.core.service;

import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;

public interface OrderService {
    //用户添加订单
    public void addOrder(Order order);

    //获取支付日志
    PayLog getPayLog(String name);

    //支付成功后修改
    void updatePayStatus(String userName);
}
