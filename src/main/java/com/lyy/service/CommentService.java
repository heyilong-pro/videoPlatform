package com.lyy.service;

import com.lyy.pojo.Comment;
import com.lyy.pojo.PageResult;

public interface CommentService {


    void add(Comment comment);

    void delete(Integer id);

    PageResult<Comment> getCommentsByCondition(Integer videoId, Integer userId, int page, int size);
}
