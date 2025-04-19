package com.lyy.pojo;

import lombok.Data;

import java.util.List;
/*接口设计
URL: GET /videos
参数：
page：页码（默认 1）
size：每页数量（默认 10）
sort：排序字段（如 createdAt,desc）
category：分类过滤
uploaderId：上传者ID过滤
status：状态过滤（如 READY）
响应：包含分页元数据和视频列表的 JSON。
*/
@Data
public class PageResult<T> {
    private Integer page;       // 当前页码
    private Integer size;       // 每页数量
    private Long total;         // 总记录数
    private Integer totalPages; // 总页数
    private List<T> items;      // 数据列表

    public PageResult(Integer page, Integer size, Long total, List<T> items) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
        this.items = items;
    }
}