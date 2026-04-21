package com.demo.boards;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.users.UserEntity;
import com.demo.users.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/boards") // 모든 요청 경로는 "/boards"로 시작
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;

    @Autowired // 생성자 주입을 통해 BoardService와 UserService를 주입
    public BoardController(BoardService boardService, UserService userService) {
        this.boardService = boardService;
        this.userService = userService;
    }

    // 전체 게시글 가져오기
    @GetMapping
    public List<BoardEntity> getBoards() {
        return boardService.getAllBoards(); // 모든 게시글을 반환
    }

    // 특정 사용자의 게시글 개수 가져오기
    @GetMapping("/count/{userId}")
    public int getPostCountByUserId(@PathVariable String userId) {
        return boardService.getPostCountByUserId(userId); // 해당 사용자의 게시글 개수 반환
    }

    // 특정 사용자의 게시글 목록 가져오기
    @GetMapping("/user/{userId}")
    public List<BoardEntity> getPostsByUserId(@PathVariable String userId) {
        return boardService.getPostsByUserId(userId); // 해당 사용자의 게시글 목록 반환
    }

    // 게시글 생성 (로그인된 사용자 정보 사용)
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody BoardEntity board, HttpSession session) {
        try {
            UserEntity loggedInUser = (UserEntity) session.getAttribute("user");
            if (loggedInUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            board.setUser(loggedInUser);
            boardService.createBoard(board, loggedInUser);
            return ResponseEntity.ok("게시글 작성 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("게시글 작성 중 오류 발생: " + e.getMessage());
        }
    }


    // Board 생성 시 로그인된 사용자의 ID를 사용하여 CRT_USER, UDT_USER 설정
    @PostMapping
    public ResponseEntity<BoardEntity> createBoard(@RequestBody BoardEntity board, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("userId");
        board.setUpdatedBy(loggedInUser); // 생성 시 사용자를 업데이트하는 사용자로 설정
        BoardEntity createdBoard = boardService.saveBoard(board, loggedInUser);
        return new ResponseEntity<>(createdBoard, HttpStatus.CREATED);
    }

    // Board 수정 시 UDT_USER 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<BoardEntity> updateBoard(@PathVariable Long id, @RequestBody BoardEntity boardDetails, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("userId");
        boardDetails.setUpdatedBy(loggedInUser); // 수정 시 업데이트한 사용자 정보 설정
        BoardEntity updatedBoard = boardService.saveBoard(boardDetails, loggedInUser);
        return ResponseEntity.ok(updatedBoard);
    }

    // 카테고리별 게시글 리스트 반환
    @GetMapping("/category/{category}")
    public List<BoardEntity> getBoardsByCategory(@PathVariable("category") String category) {
        if (category.equals("all")) {
            return boardService.getAllBoards(); // 모든 카테고리 게시글 반환
        } else {
            int categoryId = getCategoryId(category);
            return boardService.getBoardsByCategory(categoryId); // 특정 카테고리 게시글 반환
        }
    }
    
    // 카테고리 ID 변환 함수
    private int getCategoryId(String category) {
        switch (category) {
            case "free":
                return 2;
            case "questions":
                return 3;
            case "notice":
                return 4;
            default:
                return 1; // 기본값 설정
        }
    }

    // 특정 게시글 세부 조회
    @GetMapping("/detail/{boardNumber}")
    public ResponseEntity<BoardEntity> getBoardDetail(@PathVariable Long boardNumber) {
        BoardEntity board = boardService.getBoardDetail(boardNumber);
        return new ResponseEntity<>(board, HttpStatus.OK); // 200 OK 응답과 함께 게시글 반환
    }
    
    // 게시글 수정
    @PutMapping("/update/{boardNumber}")
    public ResponseEntity<BoardEntity> updateBoard(@PathVariable Long boardNumber, @RequestBody BoardEntity updatedBoard) {
        BoardEntity board = boardService.updateBoard(boardNumber, updatedBoard);
        return new ResponseEntity<>(board, HttpStatus.OK); // 200 OK 응답과 함께 수정된 게시글 반환
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{boardNumber}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardNumber) {
        boardService.deleteBoard(boardNumber); // 게시글 삭제
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content 응답
    }
}
