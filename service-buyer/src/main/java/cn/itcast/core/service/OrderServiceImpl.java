package cn.itcast.core.service;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.order.OrderQuery;
import cn.itcast.core.util.Constants;
import cn.itcast.core.util.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private PayLogDao payLogDao;
    @Override
    public void addOrder(Order order) {
        //获取当前登录用户名
        String userId = order.getUserId();
        //根据用户名从redis里面获取购物车集合
        List<BuyerCart> cartList = (List<BuyerCart>) redisTemplate.boundHashOps(Constants.CART_LIST_REDIS).get(userId);
        List<String> orderList = new ArrayList<>();//订单id集合
        double total_money=0;//总金额 （元）
        if (cartList != null) {
            //1. 遍历购物车集合
            for (BuyerCart buyerCart : cartList) {
                // TODO 根据购物车形成订单记录表
                //生成分布式id
                long orderId = idWorker.nextId();
                Order tborder = new Order();//新建订单对象
                tborder.setOrderId(orderId);//订单id
                tborder.setUserId(userId);//用户名
                tborder.setPaymentType(order.getPaymentType());//支付类型
                tborder.setStatus("1");//状态 , 未付款
                tborder.setConsignTime(new Date());//订单创建时间
                tborder.setUpdateTime(new Date());//订单修改时间
                tborder.setReceiverAreaName(order.getReceiverAreaName());//地址
                tborder.setReceiverMobile(order.getReceiverMobile());//手机号
                tborder.setReceiver(order.getReceiver());//收件人
                tborder.setSourceType(order.getSourceType());//订单来源
                tborder.setSellerId(buyerCart.getSellerId());//商家id
                double money = 0;
                //循环购物车明细
                List<OrderItem> orderItemList = buyerCart.getOrderItemList();
                // TODO 根据购物车明细生成 订单记录详情表
                if (orderItemList != null) {
                    for (OrderItem orderItem : orderItemList) {
                        //TODO 5.根据购物明细对象形成订单详情记录
                        orderItem.setId(idWorker.nextId());
                        orderItem.setOrderId( orderId  );//订单ID
                        orderItem.setSellerId(buyerCart.getSellerId());
                        money+=orderItem.getTotalFee().doubleValue();//金额累加
                        orderItemDao.insertSelective(orderItem);
                    }
                }
                tborder.setPayment(new BigDecimal(money));
                orderDao.insertSelective(tborder);
                orderList.add(orderId+"");//添加到订单列表
                total_money+=money;//累加到总金额
            }
        //TODO 计算出购物车的总价格生成订单日志表
            if ("1".equals(order.getPaymentType())) {//如果是微信支付
                PayLog payLog = new PayLog();
                String outTradeNo = idWorker.nextId() + "";//支付订单号
                payLog.setOutTradeNo(outTradeNo);
                payLog.setCreateTime(new Date());//创建时间
                //订单号列表  逗号分隔
                String ids=orderList.toString().replace("[", "").replace("]", "").replace(" ", "");
                payLog.setOrderList(ids);//订单号列表，逗号分隔
                payLog.setPayType("1");//支付类型
                payLog.setTotalFee( (long)(total_money*100 ) );//总金额(分)
                payLog.setTradeState("0");//支付状态
                payLog.setUserId(order.getUserId());//用户ID
                payLogDao.insertSelective(payLog);//插入到支付日志表
                redisTemplate.boundHashOps(Constants.CART_LIST_PAGLOG).put(order.getUserId(),payLog);//放入缓存
            }
            redisTemplate.boundHashOps(Constants.CART_LIST_CARTLIST).delete(order.getUserId());
        }

    }

    @Override
    public PayLog getPayLog(String name) {
        PayLog payLog = (PayLog)redisTemplate.boundHashOps(Constants.CART_LIST_PAGLOG).get(name);
        return payLog;
    }

    @Override
    public void updatePayStatus(String userName) {
        //根据用户获得redis的支付订单对象
        PayLog payLog = (PayLog) redisTemplate.boundHashOps(Constants.CART_LIST_PAGLOG).get(userName);
        if (payLog != null) {
            //更改支付日志的支付状态
            payLog.setTradeState("1");
            payLogDao.updateByPrimaryKeySelective(payLog);
            //更改订单表的支付状态
            String orderList = payLog.getOrderList();
            String[] split = orderList.split(",");
            if (split != null) {
                for (String s : split) {
                    Order order = new Order();
                    order.setOrderId(Long.parseLong(s));
                    order.setStatus("1");
                    orderDao.updateByPrimaryKeySelective(order);
                }
            }
            //删除redis的支付日志
            redisTemplate.boundHashOps(Constants.CART_LIST_PAGLOG).delete(userName);
        }
    }
}
