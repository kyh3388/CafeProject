package org.cloud.comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cloud.board.BoardEntity;
import org.cloud.board.BoardRepository;
import org.cloud.user.UserEntity;
import org.cloud.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            BoardRepository boardRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    // 댓글 페이지 조회 (원댓글 기준 페이징)
    public CommentPageResponseDto getCommentsByBoard(Long boardNumber, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdDate"));

        Page<CommentEntity> rootCommentPage =
                commentRepository.findByBoard_BoardNumberAndParentCommentIsNull(boardNumber, pageable);

        List<CommentEntity> rootComments = rootCommentPage.getContent();
        List<Long> rootIds = rootComments.stream()
                .map(CommentEntity::getCommentId)
                .toList();

        List<CommentEntity> childComments = rootIds.isEmpty()
                ? List.of()
                : commentRepository.findByParentComment_CommentIdInOrderByCreatedDateAsc(rootIds);

        List<Long> childIds = childComments.stream()
                .map(CommentEntity::getCommentId)
                .toList();

        List<CommentEntity> grandChildComments = childIds.isEmpty()
                ? List.of()
                : commentRepository.findByParentComment_CommentIdInOrderByCreatedDateAsc(childIds);

        // 대대댓글 맵
        java.util.Map<Long, List<CommentDto>> grandChildMap = new java.util.LinkedHashMap<>();
        for (CommentEntity grandChild : grandChildComments) {
            CommentDto dto = toCommentDto(grandChild);
            Long parentId = grandChild.getParentComment().getCommentId();
            grandChildMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(dto);
        }

        // 대댓글 맵
        java.util.Map<Long, List<CommentDto>> childMap = new java.util.LinkedHashMap<>();
        for (CommentEntity child : childComments) {
            CommentDto dto = toCommentDto(child);
            dto.setChildren(new ArrayList<>(grandChildMap.getOrDefault(child.getCommentId(), List.of())));

            Long parentId = child.getParentComment().getCommentId();
            childMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(dto);
        }

        // 원댓글 트리 조립
        List<CommentDto> rootDtos = new ArrayList<>();
        for (CommentEntity root : rootComments) {
            CommentDto dto = toCommentDto(root);
            dto.setChildren(new ArrayList<>(childMap.getOrDefault(root.getCommentId(), List.of())));
            rootDtos.add(dto);
        }

        CommentPageResponseDto response = new CommentPageResponseDto();
        response.setComments(rootDtos);
        response.setCurrentPage(rootCommentPage.getNumber());
        response.setTotalPages(rootCommentPage.getTotalPages());
        response.setTotalElements(rootCommentPage.getTotalElements());
        response.setPageSize(rootCommentPage.getSize());

        return response;
    }

    // 댓글 작성
    @Transactional
    public CommentDto createComment(Long boardNumber, CommentCreateRequestDto request, UserEntity sessionUser) {
        if (request.getCommentContent() == null || request.getCommentContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        BoardEntity board = boardRepository.findById(boardNumber)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(sessionUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        CommentEntity parentComment = null;

        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));

            if (!parentComment.getBoard().getBoardNumber().equals(boardNumber)) {
                throw new IllegalArgumentException("현재 게시글의 댓글에만 답글을 작성할 수 있습니다.");
            }

            // 깊이 제한: 대대댓글까지만 허용
            int parentDepth = getDepth(parentComment);
            if (parentDepth >= 2) {
                throw new IllegalArgumentException("대대댓글까지만 작성할 수 있습니다.");
            }
        }

        CommentEntity comment = new CommentEntity();
        comment.setBoard(board);
        comment.setUser(user);
        comment.setParentComment(parentComment);
        comment.setCommentContent(request.getCommentContent().trim());
        comment.setDeletedYn("N");
        comment.setCreatedBy(user.getUserId());
        comment.setUpdatedBy(user.getUserId());

        CommentEntity savedComment = commentRepository.save(comment);
        return toCommentDto(savedComment);
    }

    // 댓글 삭제 (소프트 삭제)
    @Transactional
    public void deleteComment(Long commentId, UserEntity sessionUser) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        boolean isWriter = comment.getUser().getUserId().equals(sessionUser.getUserId());
        boolean isAdmin = sessionUser.getUserLevel() != null && sessionUser.getUserLevel() >= 4;

        if (!isWriter && !isAdmin) {
            throw new SecurityException("본인 댓글 또는 관리자만 삭제할 수 있습니다.");
        }

        comment.setDeletedYn("Y");
        comment.setUpdatedBy(sessionUser.getUserId());
        commentRepository.save(comment);
    }

    // 댓글 depth 계산
    private int getDepth(CommentEntity comment) {
        int depth = 0;
        CommentEntity current = comment;

        while (current.getParentComment() != null) {
            depth++;
            current = current.getParentComment();
        }

        return depth;
    }

    // Entity -> DTO 변환
    private CommentDto toCommentDto(CommentEntity comment) {
        CommentDto dto = new CommentDto();
        dto.setCommentId(comment.getCommentId());
        dto.setParentCommentId(
                comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null
        );
        dto.setCommentContent(
                "Y".equals(comment.getDeletedYn()) ? "삭제된 댓글입니다." : comment.getCommentContent()
        );
        dto.setDeletedYn(comment.getDeletedYn());
        dto.setCreatedDate(comment.getCreatedDate());
        dto.setUserId(comment.getUser().getUserId());
        dto.setUserNickname(comment.getUser().getUserNickname());
        dto.setChildren(new ArrayList<>());

        return dto;
    }
}