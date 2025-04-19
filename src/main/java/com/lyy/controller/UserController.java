package com.lyy.controller;

import com.lyy.pojo.Result;
import com.lyy.pojo.User;
import com.lyy.service.UserService;
import com.lyy.utils.JwtUtil;
import com.lyy.utils.Md5Util;
import com.lyy.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username,
                           @Pattern(regexp = "^\\S{5,16}$") String password){



            //查询用户
            User u = userService.findByUsername(username);


            if (u == null) {
                userService.register(username, password);


                return Result.success();

            } else {
                return Result.error("用户名已被占用");

            }
            //注册
        }


    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username,
                                @Pattern(regexp = "^\\S{5,16}$") String password){
        //根据用户名查询用户
        User loginUser =userService.findByUsername(username);
        // 该用户是否存在
        if(loginUser==null){
            return Result.error("用户名错误");
        }
        //密码是否正确 loginUser的password是密文
        if(Md5Util.getMD5String(password).equals(loginUser.getPassword())){
            //登录成功
            Map<String,Object> claims=new HashMap<>();
            claims.put("id",loginUser.getId());
            claims.put("username",loginUser.getUsername());
            String token= JwtUtil.genToken(claims);
            //把token存储到redis中
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(token,token,24, TimeUnit.HOURS);
            return  Result.success(token);

        }
        return Result.error("密码错误");



    }
    //获取用户名
    @GetMapping("/userInfo")
    public Result<User> UserInfo(/*@RequestHeader("Authorization")String token*/){
        //解析token
       /* Map<String,Object> map=JwtUtil.parseToken(token);
        String username=(String) map.get("username");*/
        Map<String,Object> map= ThreadLocalUtil.get();
        String username=(String) map.get("username");
        User user =userService.findByUsername(username);
        return Result.success(user);

    }
    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user){
        userService.update(user);
        return Result.success();

    }
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl){
        userService.updateAvatar(avatarUrl);
        return Result.success();

    }
    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params,@RequestHeader("Authorization") String token){
        //校验参数
        String oldPwd= params.get("old_pwd");
        String newPwd= params.get("new_pwd");
        String rePwd= params.get("re_pwd");
        if(!StringUtils.hasLength(oldPwd)||!StringUtils.hasLength(newPwd)||!StringUtils.hasLength(rePwd))
        {
            return Result.error("缺少必要参数");
        }
        //原密码是否正确
        //调用userService根据用户名拿到密码，在和old_pwd比对
        Map<String,Object> map=ThreadLocalUtil.get();
        String username=(String) map.get("username");
        User loginUser= userService.findByUsername(username);

        if(!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd)))
        {
            return  Result.error("原密码错误！");
        }
      //newPwd和rePwd是否一致
        if(!rePwd.equals(newPwd)){
            return  Result.error("确认密码与新密码不一致！");
        }
        //调用service完成密码更新
        userService.updatePwd(newPwd);
        //删除redis中的token
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success();


    }
}
