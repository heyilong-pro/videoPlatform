package com.lyy.controller;

import com.lyy.pojo.PageResult;
import com.lyy.pojo.Video;
import com.lyy.pojo.Result;
import com.lyy.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/video")
public class VideoController {
    @Autowired
    private VideoService videoService;


    @GetMapping("/info")
    public Result<Video> videoInfo(@RequestParam Integer id){
        Video video= videoService.findById(id);
        return Result.success(video);
    }

    @Transactional
    @DeleteMapping
    public Result Delete(@RequestParam Integer id){
        Video video = videoService.findById(id);
        if(video ==null)
            return Result.error("视频不存在!");
        try {
            videoService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败，系统异常");
        }
    }
    @GetMapping("/list")
    public Result<PageResult<Video>> listVideos(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer uploaderId,
            @RequestParam(required = false) String status
    ) {
        PageResult<Video> result = videoService.listVideos(
                page, size, sort, category, uploaderId, status
        );
        return Result.success(result);
    }

    @GetMapping("/search")
    public Result<PageResult<Video>> searchVideos(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        // 关键词去空格处理
        String keyword = q.trim();
        if (keyword.isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }
        PageResult<Video> result = videoService.searchVideos(keyword, page, size);
        return Result.success(result);
    }
}
