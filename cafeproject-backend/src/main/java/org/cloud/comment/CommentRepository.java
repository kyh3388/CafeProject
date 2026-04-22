package org.cloud.comment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 원댓글만 페이지 단위로 조회
    Page<CommentEntity> findByBoard_BoardNumberAndParentCommentIsNull(
            Long boardNumber,
            Pageable pageable
    );

    // 여러 부모 댓글의 자식 댓글을 한 번에 조회
    List<CommentEntity> findByParentComment_CommentIdInOrderByCreatedDateAsc(List<Long> parentCommentIds);
}