package com.lyy.controller;

import com.lyy.pojo.Result;
import com.lyy.pojo.Video;
import com.lyy.service.VideoService;
import com.lyy.utils.AliOssUtil;
import com.lyy.utils.VideoUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class FileUploadController {
    @Autowired
    private VideoService videoService;


    @PostMapping("/upload")
    public Result<Video> uploadVideoAndCover(
            @RequestParam("video") MultipartFile videoFile,       // 视频文件（必传）
            @RequestParam(value = "cover", required = false) MultipartFile coverFile, // 封面（可选）
            @RequestParam String title,                           // 视频标题
            @RequestParam String description,                   // 视频描述
            @RequestParam(required = false) List<String> tags,
            @RequestParam String category // 直接传分类名称

    ) throws Exception {
        // 1. 上传视频到阿里云 OSS
        String videoUrl = uploadToOSS(videoFile, "videos/");

        // 2. 上传封面（如果存在）
        String coverUrl = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = uploadToOSS(coverFile, "covers/");
        }
        if (coverUrl == null) {
            coverUrl = VideoUtils.generateCover(videoFile);
        }
        // 3. 构建 Video 对象
        Video video = new Video();

        video.setTitle(title);
        video.setDescription(description);
        video.setVideoUrl(videoUrl);
        video.setCoverImg(coverUrl);  // 封面可能为 null
        video.setStatus(Video.VideoStatus.PROCESSING);  // 初始状态为处理中
        video.setCategory(category);

        // 4. 保存到数据库

        videoService.addVideoWithTags(video,videoFile,tags);
        // 5. 返回统一响应结果
        return Result.success(video);
    }

    // 通用上传方法
    private String uploadToOSS(MultipartFile file, String ossDirectory) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String ossFileName = ossDirectory + UUID.randomUUID() + fileExtension; // 如 "videos/abc123.mp4"
        return AliOssUtil.uploadFile(ossFileName, file.getInputStream());
    }


    @PutMapping("/update")
    public Result<Video> update(@RequestParam Integer id,
                                @RequestParam(value = "title", required = false) String title,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam(value = "category", required = false) String category,
                                @RequestParam(value = "status", required = false) Video.VideoStatus status,
                                @RequestParam(value = "cover", required = false) MultipartFile coverFile,
                                @RequestParam(value = "video", required = false) MultipartFile videoFile) throws Exception {
        Video video=videoService.findById(id);
        if (video!= null) {

            // 2. 更新基础字段
            if (title != null) video.setTitle(title);
            if (description != null) video.setDescription(description);
            if (category != null) video.setCategory(category);
            if (status != null) video.setStatus(status);

            //  处理文件更新
            if (coverFile != null && !coverFile.isEmpty()) {
                String newCoverUrl = uploadToOSS(coverFile, "covers/");
                video.setCoverImg(newCoverUrl);
            }

            if (videoFile != null && !videoFile.isEmpty()) {
                // 重新上传视频并提取元数据
                String newVideoUrl = uploadToOSS(videoFile, "videos/");
                video.setVideoUrl(newVideoUrl);
                video.setDuration(VideoUtils.extractVideoDuration(videoFile));
                video.setFileSize((int) videoFile.getSize());
                // 重新生成封面（可选）
                if (coverFile == null) {
                    String autoCoverUrl = VideoUtils.generateCover(videoFile);
                    video.setCoverImg(autoCoverUrl);
                }
            }
            videoService.update(video);

        }
        else {
            return   Result.error("视频不存在");

        }
        return Result.success();
    }

}
