package com.lyy.mapper;

import com.lyy.pojo.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    @Insert("insert into video_comments (content, user_id, video_id, created_at) VALUE (#{content},#{userId},#{videoId},#{createdAt}) ")
    void add(Comment comment);
    @Delete("delete from video_comments where id=#{id}")
    void delete(Integer id);

    List<Comment> selectCommentsByCondition( @Param("videoId") Integer videoId,
                                             @Param("userId") Integer userId,
                                             @Param("offset") int offset,
                                             @Param("size") int size);

    Long countCommentsByCondition(@Param("videoId") Integer videoId,
                                  @Param("userId") Integer userId);
}
