package org.cloud.comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CommentDto {

    private Long commentId;
    private Long parentCommentId;
    private String commentContent;
    private String deletedYn;
    private LocalDateTime createdDate;
    private String userId;
    private String userNickname;
    private List<CommentDto> children = new ArrayList<>();
}