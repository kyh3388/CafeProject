package org.cloud.board;

import java.util.List;

import org.cloud.user.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 전체 게시글 가져오기
    @GetMapping
    public List<BoardDto> getBoards() {
        return boardService.getAllBoardDtos();
    }

    // 특정 사용자의 게시글 개수 가져오기
    @GetMapping("/count/{userId}")
    public int getPostCountByUserId(@PathVariable String userId) {
        return boardService.getPostCountByUserId(userId);
    }

    // 특정 사용자의 게시글 목록 가져오기
    @GetMapping("/user/{userId}")
    public List<BoardDto> getPostsByUserId(@PathVariable String userId) {
        return boardService.getPostDtosByUserId(userId);
    }

    // 게시글 생성 (로그인된 사용자 정보 사용)
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody BoardEntity board, HttpSession session) {
        try {
        	UserEntity loggedInUser = (UserEntity) session.getAttribute("user");
        	if (loggedInUser == null) {
        	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        	}

        	if (board.getBoardCategory() == 4 && loggedInUser.getUserLevel() < 4) {
        	    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        	            .body("공지글은 관리자(LV4)만 작성할 수 있습니다.");
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
    public ResponseEntity<?> createBoard(@RequestBody BoardEntity board, HttpSession session) {
        UserEntity loggedInUser = (UserEntity) session.getAttribute("user");

        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        if (board.getBoardCategory() == 4 && loggedInUser.getUserLevel() < 4) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("공지글은 관리자(LV4)만 작성할 수 있습니다.");
        }

        board.setUser(loggedInUser);
        board.setUpdatedBy(loggedInUser.getUserId());
        BoardEntity createdBoard = boardService.saveBoard(board, loggedInUser.getUserId());
        return new ResponseEntity<>(boardService.toBoardDto(createdBoard), HttpStatus.CREATED);
    }

    // Board 수정 시 UDT_USER 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<BoardDto> updateBoard(@PathVariable Long id, @RequestBody BoardEntity boardDetails, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("userId");
        boardDetails.setUpdatedBy(loggedInUser);
        BoardEntity updatedBoard = boardService.saveBoard(boardDetails, loggedInUser);
        return ResponseEntity.ok(boardService.toBoardDto(updatedBoard));
    }

    // 카테고리별 게시글 리스트 반환
    @GetMapping("/category/{category}")
    public List<BoardDto> getBoardsByCategory(@PathVariable("category") String category) {
        if (category.equals("all")) {
            return boardService.getAllBoardDtos();
        } else {
            int categoryId = getCategoryId(category);
            return boardService.getBoardDtosByCategory(categoryId);
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
                return 1;
        }
    }

    // 특정 게시글 세부 조회
    @GetMapping("/detail/{boardNumber}")
    public ResponseEntity<BoardDto> getBoardDetail(@PathVariable Long boardNumber) {
        BoardDto board = boardService.getBoardDetailDto(boardNumber);
        return new ResponseEntity<>(board, HttpStatus.OK);
    }

    // 게시글 수정
    @PutMapping("/update/{boardNumber}")
    public ResponseEntity<BoardDto> updateBoard(@PathVariable Long boardNumber, @RequestBody BoardEntity updatedBoard) {
        BoardEntity board = boardService.updateBoard(boardNumber, updatedBoard);
        return new ResponseEntity<>(boardService.toBoardDto(board), HttpStatus.OK);
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{boardNumber}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardNumber) {
        boardService.deleteBoard(boardNumber);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}