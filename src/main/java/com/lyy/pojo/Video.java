package com.lyy.pojo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lyy.anno.State;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Video {
    @Id
    private Integer id;//主键ID
    @NotEmpty
    @Pattern(regexp = "^\\S{1,10}$")
    private String title;//视频标题
    @Lob  //视频简介
    private String description;
    @Column(nullable = false, length = 50)
    private String category; // 直接存储分类名称（如 "科技"、"教育"）
    @NotEmpty
    @URL
    private String videoUrl;// 视频文件存储路径（如OSS/S3/MinIO的URL）
    @NotEmpty
    @URL
    private String coverImg;//封面图像
    @State

    private Integer duration;        // 视频时长（单位：秒）
    private Integer fileSize;       // 文件大小（字节）

    @Enumerated(EnumType.STRING)
    private VideoStatus status = VideoStatus.PROCESSING; // 默认转码中

    @ElementCollection
    @CollectionTable(name = "video_tags", joinColumns = @JoinColumn(name = "video_id"))
    private List<String> tags = new ArrayList<>(); // 标签（或关联分类实体）

    @Column(name = "view_count", columnDefinition = "INT DEFAULT 0")
    private Integer viewCount = 0; // 播放量

    @Column(name = "like_count", columnDefinition = "INT DEFAULT 0")
    private Integer likeCount = 0; // 点赞数

    @Column(name = "favorite_count", columnDefinition = "INT DEFAULT 0")
    private Integer favoriteCount = 0; // 收藏数



    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    private Integer uploaderId;//创建人ID
    // JPA 生命周期回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 枚举定义视频状态
    public enum VideoStatus {
        PROCESSING,  // 转码中
        READY,       // 可播放
        DELETED      // 已删除
    }
}
