package com.lyy.mapper;

import com.lyy.pojo.Video;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;


@Mapper
public interface VideoMapper {
    //新增
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into videos(title,category,description,uploader_id,video_url,coverImg,duration,file_size,status,view_count,like_count,favorite_count,created_at,updated_at)" +
            "values (#{title},#{category},#{description},#{uploaderId},#{videoUrl},#{coverImg},#{duration},#{fileSize},#{status},#{viewCount},#{likeCount},#{favoriteCount},#{createdAt}, #{updatedAt})")
    void add(Video video);

    void addTags(@Param("videoId") Integer videoId, @Param("tags") List<String> tags);

    void update(Video video);
    @Select("select * from videos where id=#{id}")
    Video findById(Integer id);
    @Delete("delete from videos where id=#{id}")
    void delete(Integer id);


    // 分页查询视频列表（直接返回 Video 实体）
    List<Video> listVideos(
            @Param("category") String category,
            @Param("uploaderId") Integer uploaderId,
            @Param("status") String status,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("size") int size
    );

    // 查询总记录数
    Long countVideos(
            @Param("category") String category,
            @Param("uploaderId") Integer uploaderId,
            @Param("status") String status
    );

    // 批量查询标签（根据视频ID列表）
    List<Map<String, Object>> batchGetTags(@Param("videoId") List<Integer> videoId);

    List<Video> searchVideos(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("size") int size
    );

    Long countSearchVideos(@Param("keyword") String keyword);

}
