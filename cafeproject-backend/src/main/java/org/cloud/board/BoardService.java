package org.cloud.board;

import java.time.LocalDateTime;
import java.util.List;

import org.cloud.user.UserDto;
import org.cloud.user.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    // 모든 게시글 Entity 조회
    public List<BoardEntity> getAllBoards() {
        return boardRepository.findAll();
    }

    // 모든 게시글 DTO 조회
    public List<BoardDto> getAllBoardDtos() {
        return boardRepository.findAll()
                .stream()
                .map(this::toBoardDto)
                .toList();
    }

    // Entity -> DTO 변환
    public BoardDto toBoardDto(BoardEntity board) {
        if (board == null) {
            return null;
        }

        BoardDto dto = new BoardDto();
        dto.setBoardNumber(board.getBoardNumber());
        dto.setBoardCategory(board.getBoardCategory());
        dto.setBoardTitle(board.getBoardTitle());
        dto.setBoardWrite(board.getBoardWrite());
        dto.setCreatedDate(board.getCreatedDate());
        dto.setUpdatedDate(board.getUpdatedDate());
        dto.setUser(toUserDto(board.getUser()));

        return dto;
    }

    // 게시글 안의 작성자 정보도 DTO로 변환
    private UserDto toUserDto(UserEntity user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setUserNickname(user.getUserNickname());
        dto.setUserLevel(user.getUserLevel());
        dto.setUserImage(user.getUserImage());
        dto.setUserImageType(user.getUserImageType());
        return dto;
    }

    // 특정 사용자의 게시글 개수 반환
    public int getPostCountByUserId(String userId) {
        return boardRepository.countPostsByUserId(userId);
    }

    // 특정 사용자의 게시글 Entity 조회
    public List<BoardEntity> getPostsByUserId(String userId) {
        return boardRepository.findByUserUserId(userId);
    }

    // 특정 사용자의 게시글 DTO 조회
    public List<BoardDto> getPostDtosByUserId(String userId) {
        return boardRepository.findByUserUserId(userId)
                .stream()
                .map(this::toBoardDto)
                .toList();
    }

    // 게시글 저장 (생성 또는 수정)
    @Transactional
    public BoardEntity saveBoard(BoardEntity board, String loggedInUser) {
        if (board.getBoardNumber() == null) {
            board.setCreatedBy(loggedInUser);
        }
        board.setUpdatedBy(loggedInUser);
        board.setUpdatedDate(LocalDateTime.now());
        return boardRepository.save(board);
    }

    // 게시글 생성
    public void createBoard(BoardEntity board, UserEntity user) {
        board.setUser(user);
        board.setCreatedDate(LocalDateTime.now());
        board.setUpdatedDate(LocalDateTime.now());
        board.setCreatedBy(user.getUserId());
        board.setUpdatedBy(user.getUserId());
        boardRepository.save(board);
    }

    // 카테고리별 Entity 조회
    public List<BoardEntity> getBoardsByCategory(int category) {
        return boardRepository.findByBoardCategory(category);
    }

    // 카테고리별 DTO 조회
    public List<BoardDto> getBoardDtosByCategory(int category) {
        return boardRepository.findByBoardCategory(category)
                .stream()
                .map(this::toBoardDto)
                .toList();
    }

    // 특정 게시글 Entity 조회
    public BoardEntity getBoardById(Long boardNumber) {
        return boardRepository.findById(boardNumber).orElse(null);
    }

    // 특정 게시글 상세 Entity 조회
    public BoardEntity getBoardDetail(Long boardNumber) {
        return boardRepository.findById(boardNumber)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));
    }

    // 특정 게시글 상세 DTO 조회
    public BoardDto getBoardDetailDto(Long boardNumber) {
        BoardEntity board = boardRepository.findById(boardNumber)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));
        return toBoardDto(board);
    }

    // 게시글 수정
    public BoardEntity updateBoard(Long boardNumber, BoardEntity updatedBoard) {
        BoardEntity board = boardRepository.findById(boardNumber)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));
        board.setBoardTitle(updatedBoard.getBoardTitle());
        board.setBoardWrite(updatedBoard.getBoardWrite());
        board.setUpdatedDate(LocalDateTime.now());
        return boardRepository.save(board);
    }

    // 게시글 삭제
    public void deleteBoard(Long boardNumber) {
        boardRepository.deleteById(boardNumber);
    }
}