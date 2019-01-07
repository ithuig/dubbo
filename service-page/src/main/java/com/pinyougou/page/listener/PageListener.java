package com.pinyougou.page.listener;

import cn.itcast.core.service.CmsService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

public class PageListener implements MessageListener {
    @Autowired
    CmsService cmsService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage act = (ActiveMQTextMessage) message;
        try {
            String text = act.getText();
            Map<String, Object> goodDate = cmsService.findGoodDate(Long.parseLong(text));
            cmsService.createStaticPage(goodDate,Long.parseLong(text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
