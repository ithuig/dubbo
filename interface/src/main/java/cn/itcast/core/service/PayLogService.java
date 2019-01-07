package cn.itcast.core.service;

import java.util.Map;

public interface PayLogService {

    Map createNative(String outTradeNo, String totolFee);

    Map<String, String> queryPayStatus(String out_trade_no);

}
