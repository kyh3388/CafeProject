package com.demo.boards;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.users.UserEntity;

@Service // 이 클래스가 서비스 레이어의 컴포넌트임을 나타냄
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    // 모든 게시글 리스트 반환
    public List<BoardEntity> getAllBoards() {
        return boardRepository.findAll(); // 모든 게시글을 데이터베이스에서 조회하여 반환
    }

    // 특정 사용자의 게시글 개수 반환
    public int getPostCountByUserId(String userId) {
        return boardRepository.countPostsByUserId(userId); // 사용자 ID에 해당하는 게시글 개수 반환
    }

    // 특정 사용자의 게시글 리스트 반환
    public List<BoardEntity> getPostsByUserId(String userId) {
        return boardRepository.findByUserUserId(userId); // 사용자 ID에 해당하는 모든 게시글 반환
    }

    // 게시글 저장 (생성 또는 수정)
    @Transactional
    public BoardEntity saveBoard(BoardEntity board, String loggedInUser) {
        if (board.getBoardNumber() == null) { // 새 게시글인 경우
            board.setCreatedBy(loggedInUser);
        }
        board.setUpdatedBy(loggedInUser);
        board.setUpdatedDate(LocalDateTime.now());
        return boardRepository.save(board);
    }


    // 게시글 생성
    public void createBoard(BoardEntity board, UserEntity user) {
        board.setUser(user); // 게시글의 작성자 설정
        board.setCreatedDate(LocalDateTime.now()); // 현재 시간을 생성 날짜로 설정
        board.setUpdatedDate(LocalDateTime.now()); // 현재 시간을 수정 날짜로 설정
        board.setCreatedBy(user.getUserId()); // 작성자를 사용자 ID로 설정
        board.setUpdatedBy(user.getUserId()); // 수정자를 사용자 ID로 설정
        boardRepository.save(board); // 게시글 저장
    }

    // 카테고리별 게시물 목록 반환
    public List<BoardEntity> getBoardsByCategory(int category) {
        return boardRepository.findByBoardCategory(category); // 카테고리에 해당하는 모든 게시글 반환
    }

    // 특정 게시글 ID로 게시글 반환
    public BoardEntity getBoardById(Long boardNumber) {
        return boardRepository.findById(boardNumber).orElse(null); // ID로 게시글 조회, 없으면 null 반환
    }

    // 게시글 상세 정보 반환
    public BoardEntity getBoardDetail(Long boardNumber) {
        return boardRepository.findById(boardNumber)
            .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다.")); // 게시글 조회, 없으면 예외 발생
    }

    // 게시글 수정
    public BoardEntity updateBoard(Long boardNumber, BoardEntity updatedBoard) {
        BoardEntity board = boardRepository.findById(boardNumber)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다.")); // 게시글 조회, 없으면 예외 발생
        board.setBoardTitle(updatedBoard.getBoardTitle()); // 제목 업데이트
        board.setBoardWrite(updatedBoard.getBoardWrite()); // 내용 업데이트
        board.setUpdatedDate(LocalDateTime.now()); // 수정 날짜 갱신
        return boardRepository.save(board); // 게시글 저장
    }

    // 게시글 삭제
    public void deleteBoard(Long boardNumber) {
        boardRepository.deleteById(boardNumber); // ID로 게시글 삭제
    }
}
