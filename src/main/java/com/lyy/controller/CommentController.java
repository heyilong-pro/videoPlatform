package com.lyy.controller;

import com.lyy.pojo.Comment;
import com.lyy.pojo.PageResult;
import com.lyy.pojo.Result;
import com.lyy.service.CommentService;
import com.lyy.service.UserService;
import com.lyy.service.VideoService;
import com.lyy.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    CommentService commentService;
    VideoService videoService;
    UserService userService;
    @PostMapping()
    public Result<Comment> addComment(@RequestParam String content,
                                      @RequestParam Integer videoId
    ) {
        Comment comment=new Comment();
        comment.setContent(content);
        comment.setVideoId(videoId);
        Map<String,Object> map= ThreadLocalUtil.get();
        Integer id=(Integer) map.get("id");
        comment.setUserId(id);
        comment.setCreatedAt(LocalDateTime.now());
        commentService.add(comment);
        return Result.success(comment);
    }

    @GetMapping
    public Result<PageResult<Comment>> getComments(
            @RequestParam(required = false) Integer videoId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 校验至少有一个过滤条件
        if (videoId == null && userId == null) {
            return Result.error("无参数"); // 或自定义错误响应
        }

        PageResult<Comment> result = commentService.getCommentsByCondition(videoId, userId, page, size);
        return Result.success(result);
    }

    @DeleteMapping()
    public Result deleteComment(@RequestParam Integer id){
        if(id==null){
            return Result.error("评论不存在！");
        }
        commentService.delete(id);
        return Result.success();
    }
}
