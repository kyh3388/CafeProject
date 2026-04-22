package org.cloud.comment;

import java.util.List;

import lombok.Data;

@Data
public class CommentPageResponseDto {

    private List<CommentDto> comments;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}