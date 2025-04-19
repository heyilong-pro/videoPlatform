package com.lyy.interceptors;

import com.lyy.pojo.Result;
import com.lyy.utils.JwtUtil;
import com.lyy.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;


@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //令牌验证
        String token =request.getHeader("Authorization");
        try {
            //从redis中获取相同的token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redistoken=operations.get(token);
            if(redistoken==null) {
                //token已失效
                throw new RuntimeException();
            }

            Map<String,Object> claims= JwtUtil.parseToken(token);
            //把业务数据存入Threadlocal中
            ThreadLocalUtil.set(claims);
            //放行
            return true;
        } catch (Exception e) {
            //http响应码为401
            response.setStatus(401);
            //不放行
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空Threadlocal中的数据
        ThreadLocalUtil.remove();
    }
}
