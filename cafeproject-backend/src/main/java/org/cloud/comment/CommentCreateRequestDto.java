package org.cloud.comment;

import lombok.Data;

@Data
public class CommentCreateRequestDto {

    private Long parentCommentId;
    private String commentContent;
}