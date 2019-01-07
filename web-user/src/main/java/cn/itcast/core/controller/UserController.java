package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import cn.itcast.core.util.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    UserService userService;

    //短信验证
    @RequestMapping("/sendCode")
    public Result sendCode(String phone) {
        try {
            if (phone == null || "".equals(phone)) {
                return new Result(false, "请输入手机号");
            }
            if (!PhoneFormatCheckUtils.isChinaPhoneLegal(phone)) {
                return new Result(false, "请输入正确的手机号");
            }
            userService.sendCode(phone);
            return new Result(true, "发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "发送失败");
        }
    }

    //用户注册,传入用户信息和输入的短信验证码
    @RequestMapping("/add")
    public Result add(@RequestBody User user, String smscode) {
        try {
            //初始化用户对象信息
            user.setCreated(new Date());
            user.setUpdated(new Date());
            user.setSourceType("1");
            user.setStatus("Y");
            //验证用户输入的验证码是否正确
            if (!userService.checkSmsCode(user.getPhone(), smscode)) {
                return new Result(false, "手机号或者验证码输入错误");
            }
            userService.add(user);
            return new Result(true, "注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "注册失败");
        }
    }
}
