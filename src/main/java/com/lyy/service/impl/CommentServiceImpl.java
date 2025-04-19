package com.lyy.service.impl;

import com.lyy.mapper.CommentMapper;
import com.lyy.pojo.Comment;
import com.lyy.pojo.PageResult;
import com.lyy.pojo.Result;
import com.lyy.service.CommentService;
import com.lyy.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public void add(Comment comment) {
        Map<String,Object> map= ThreadLocalUtil.get();
        Integer id=(Integer) map.get("id");
        comment.setUserId(id);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.add(comment);

    }

    @Override
    public void delete(Integer id) {

        commentMapper.delete(id);
    }

    @Override
        public PageResult<Comment> getCommentsByCondition(Integer videoId, Integer userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Comment> comments = commentMapper.selectCommentsByCondition(videoId, userId, offset, size);
        Long total = commentMapper.countCommentsByCondition(videoId, userId);
        return new PageResult<>(page, size, total, comments);
    }
}
