package com.lyy.service.impl;

import com.lyy.mapper.VideoMapper;
import com.lyy.pojo.PageResult;
import com.lyy.pojo.Video;
import com.lyy.service.VideoService;
import com.lyy.utils.AliOssUtil;
import com.lyy.utils.ThreadLocalUtil;
import com.lyy.utils.VideoUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoMapper videoMapper;
    @Override
    public void add(Video video,MultipartFile videoFile) throws IOException {
        // 1. 设置文件大小
        video.setFileSize((int) videoFile.getSize());

        // 2. 提取视频时长
        int duration = VideoUtils.extractVideoDuration(videoFile);
        video.setDuration(duration);

        // 3. 初始化播放次数为 0
        video.setViewCount(0);
        video.setLikeCount(0);
        video.setFavoriteCount(0);
        // 4. 设置上传人id
        Map<String,Object> map= ThreadLocalUtil.get();
        Integer userid=(Integer) map.get("id");
        video.setUploaderId(userid);
        video.setCreatedAt(LocalDateTime.now());
        video.setUpdatedAt(LocalDateTime.now());
        videoMapper.add(video);

    }
    @Transactional  // 确保事务一致性
    @Override
    public void addVideoWithTags(Video video, MultipartFile videoFile, List<String> tags) throws IOException {
        // 1. 设置文件大小
        video.setFileSize((int) videoFile.getSize());

        // 2. 提取视频时长
        int duration = VideoUtils.extractVideoDuration(videoFile);
        video.setDuration(duration);

        // 3. 初始化播放次数为 0
        video.setViewCount(0);
        video.setLikeCount(0);
        video.setFavoriteCount(0);
        // 4. 设置上传人id
        Map<String,Object> map= ThreadLocalUtil.get();
        Integer userid=(Integer) map.get("id");
        video.setUploaderId(userid);
        video.setCreatedAt(LocalDateTime.now());
        video.setUpdatedAt(LocalDateTime.now());
        // 1. 插入视频主表数据
        videoMapper.add(video);

        // 2. 去重并插入标签（如果存在）
        if (tags != null && !tags.isEmpty()) {
            List<String> uniqueTags = tags.stream()
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            if (!uniqueTags.isEmpty()) {
                videoMapper.addTags(video.getId(), uniqueTags);
            }
        }
    }


    @Override
    public void update(Video video) {

        video.setUpdatedAt(LocalDateTime.now());
        videoMapper.update(video);
    }

    @Override
    public Video findById(Integer id) {
        Video video = videoMapper.findById(id);
        return video;
    }

    @Override
    public void delete(Integer id) {
        Video video = videoMapper.findById(id);
        // 2. 删除阿里云 OSS 文件（视频和封面）
        AliOssUtil.deleteOssFile(video.getVideoUrl()); // 根据存储的URL解析objectKey
        AliOssUtil.deleteOssFile(video.getCoverImg());
        videoMapper.delete(id);
        // 4. （可选）手动删除评论等非级联数据
        /*commentMapper.deleteByVideoId(id);*/
    }

    @Override
    public PageResult<Video> listVideos(Integer page, Integer size, String sort, String category, Integer uploaderId, String status) {
        // 1. 计算分页偏移
        int offset = (page - 1) * size;

        // 2. 动态排序处理（如 "createdAt,desc" -> "created_at DESC"）
        String parsedSort = parseSort(sort);

        // 3. 查询分页数据
        List<Video> videos = videoMapper.listVideos(
                category, uploaderId, status, parsedSort, offset, size
        );

        // 4. 批量查询标签并填充
        if (!videos.isEmpty()) {
            List<Integer> videoIds = videos.stream()
                    .map(Video::getId)
                    .collect(Collectors.toList());

            List<Map<String, Object>> tagMaps = videoMapper.batchGetTags(videoIds);
            Map<Long, List<String>> tagsByVideoId = tagMaps.stream()
                    .collect(Collectors.groupingBy(
                            map -> (Long) map.get("videoId"),
                            Collectors.mapping(map -> (String) map.get("tag"), Collectors.toList())
                    ));

            videos.forEach(video ->
                    video.setTags(tagsByVideoId.getOrDefault(video.getId(), new ArrayList<>()))
            );
        }

        // 5. 查询总数
        Long total = videoMapper.countVideos(category, uploaderId, status);

        return new PageResult<>(page, size, total, videos);
    }

    @Override
    public PageResult<Video> searchVideos(String keyword, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Video> videos = videoMapper.searchVideos(keyword, offset, size);
        Long total = videoMapper.countSearchVideos(keyword);
        return new PageResult<>(page, size, total, videos);
    }

    // 排序字段转换（驼峰转下划线）
    private String parseSort(String sort) {
        if (sort == null) return "created_at DESC";
        String[] parts = sort.split(",");
        String field = parts[0].replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        String direction = parts.length > 1 ? parts[1] : "DESC";
        return field + " " + direction;
    }


}
