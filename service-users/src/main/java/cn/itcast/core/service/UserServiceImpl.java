package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    JmsTemplate jmsTemplate;
    //点对点发送,,像这个目标发送
    @Autowired
    ActiveMQQueue smsDestination;

    @Value("${template_code}")
    String template_code;

    @Value("${sign_name}")
    private String sign_name;

    @Autowired
    UserDao userDao;
    /**
     * //生成6位随机数
     */
    public static int randomCode() {
        return (int) ((Math.random() * 9 + 1) * 100000);
    }

    @Override
    public void sendCode(final String phone) {
        //生成六位数验证码
        final String s = String.valueOf(randomCode());
        System.out.println("验证码为:"+s);
        //2.将手机号作为key,验证码作为value存入到redis数据库中,有效时间十分钟;
        redisTemplate.boundValueOps(phone).set(s, 60 * 10, TimeUnit.SECONDS);
        //将手机号,验证码,模板编号,签名封装成Map类型消息发送给消息服务器
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage message = session.createMapMessage();
                message.setString("phone", phone);
                message.setString("sign_name", sign_name);
                message.setString("template_code", template_code);
                Map map = new HashMap<>();
                map.put("code", s);
                message.setString("param", JSON.toJSONString(map));
                return message;
            }
        });
    }

    @Override
    public Boolean checkSmsCode(String phone,String smscode) {
        //判断手机号码和手机验证码是否为空,为空直接返回
        if (phone == null || "".equals(phone) || smscode == null || "".equals(smscode)) {
            return false;
        }
        //根据手机号从redis里面获取验证码:
        String redisSmsCode = (String) redisTemplate.boundValueOps(phone).get();
        //判断页面传来的验证码与自己存的是否一致
        if (smscode.equals(redisSmsCode)) {
            return true;
        }
        return false;
    }

    @Override
    public void add(User user) {
        userDao.insertSelective(user);
    }
}
