package com.lyy.service;

import com.lyy.pojo.PageResult;
import com.lyy.pojo.Video;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VideoService {
    //添加文章
    void add(Video video, MultipartFile videoFile) throws IOException;

    @Transactional
        // 确保事务一致性
    void addVideoWithTags(Video video, MultipartFile videoFile, List<String> tags) throws IOException;

    void update(Video video);

    Video findById(Integer id);

    void delete(Integer id);


    PageResult<Video> listVideos(Integer page, Integer size, String sort, String category, Integer uploaderId, String status);

    PageResult<Video> searchVideos(String keyword, Integer page, Integer size);
}
