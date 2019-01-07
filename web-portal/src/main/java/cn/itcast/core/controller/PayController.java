package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.service.OrderService;
import cn.itcast.core.service.PayLogService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private OrderService orderService;
    @Reference
    private PayLogService payLogService;

    @RequestMapping("/createNative")
    public Map createNative() {
        //获取当前用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //获取订单支付日志
        PayLog payLog = orderService.getPayLog(name);
        if (payLog != null) {
            //调用统一下单接口,生成支付连接
            Map aNative = payLogService.createNative(payLog.getOutTradeNo(), "1");
            return aNative;
        }
        return new HashMap();
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int flag = 1;
        while (true) {
            //1. 根据支付单号调用查询订单接口, 查询是否支付成功
            Map<String, String> map = payLogService.queryPayStatus(out_trade_no);
            if (map == null) {
                result = new Result(false, "二维码超时");
                break;
            }
            //判断支付是否成功
            if ("SUCCESS".equals(map.get("trade_state"))) {
                result = new Result(true, "支付成功");
                //4. 如果支付成功, 删除支付日志记录, 将支付状态改为已支付
                orderService.updatePayStatus(userName);
                break;
            }
            //每三秒查一次
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //如果等待扫码时间超过5分钟, 则返回支付失败, 二维码超时, 页面重新调用统一下单接口重新生成一个二维码.
            if (flag > 100) {
                result = new Result(false, "二维码超时");
                break;
            }
            flag++;
        }
        return result;
    }
}
