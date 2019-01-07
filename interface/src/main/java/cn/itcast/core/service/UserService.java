package cn.itcast.core.service;

import cn.itcast.core.pojo.user.User;

public interface UserService {
    //根据手机号发送短信
    public void sendCode(String phone);

    //验证用户验证码是否正确
    public Boolean checkSmsCode(String phone,String smscode);

    //添加用户信息到数据库
    public void add(User user);
}
