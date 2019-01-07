package cn.itcast.core.listener;

import cn.itcast.core.service.SaveManageToSolr;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemDeleteListener implements MessageListener {
    @Autowired
    SaveManageToSolr saveManageToSolr;

    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage act = (ActiveMQTextMessage) message;
        try {
            String text = act.getText();
            //根据商品id删除solr索引库中对应的数据
            saveManageToSolr.deleteSolr(Long.parseLong(text));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
