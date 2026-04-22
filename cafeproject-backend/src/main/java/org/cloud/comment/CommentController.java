package org.cloud.comment;

import org.cloud.user.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 게시글 댓글 조회 (원댓글 기준 페이징)
    @GetMapping("/boards/{boardNumber}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long boardNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            CommentPageResponseDto response = commentService.getCommentsByBoard(boardNumber, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("댓글 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 댓글 작성
    @PostMapping("/boards/{boardNumber}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable Long boardNumber,
            @RequestBody CommentCreateRequestDto request,
            HttpSession session
    ) {
        try {
            UserEntity sessionUser = (UserEntity) session.getAttribute("user");
            if (sessionUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            CommentDto createdComment = commentService.createComment(boardNumber, request, sessionUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("댓글 작성 중 오류 발생: " + e.getMessage());
        }
    }

    // 댓글 삭제 (소프트 삭제)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpSession session) {
        try {
            UserEntity sessionUser = (UserEntity) session.getAttribute("user");
            if (sessionUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            commentService.deleteComment(commentId, sessionUser);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("댓글 삭제 중 오류 발생: " + e.getMessage());
        }
    }
}