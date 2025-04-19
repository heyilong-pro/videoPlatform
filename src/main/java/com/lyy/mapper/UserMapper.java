package com.lyy.mapper;

import com.lyy.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface UserMapper {
    //用户名查询用户
    @Select("select * from user where username=#{username}")
    User findByUsername(String username);
    //添加
    @Insert("insert into user(username,password,create_time,update_time)"+"value (#{username},#{password},now(),now())")
    void add(String username, String password);
    //更新
    @Update("update user set nickname=#{nickname},email=#{email},update_time=#{updateTime} where id=#{id}")
    void update(User user);

    @Update("update user set user_pic=#{avatarUrl},update_time=now() where id=#{id}")
    void updateAvatar(String avatarUrl,Integer id);

    @Update("update  user set password=#{newPwd},update_time=now() where id=#{id}")
    void updatePwd(String newPwd,Integer id);
}
