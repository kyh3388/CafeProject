package com.demo.boards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    // 특정 사용자의 게시글 개수를 반환하는 쿼리
    @Query("SELECT COUNT(b) FROM BoardEntity b WHERE b.user.userId = :userId")
    int countPostsByUserId(@Param("userId") String userId);

    // 특정 사용자가 작성한 게시글 목록을 가져오는 메서드
    @Query("SELECT b FROM BoardEntity b WHERE b.user.userId = :userId")
    List<BoardEntity> findByUserUserId(@Param("userId") String userId);

    // 카테고리별 게시글 리스트 반환
    List<BoardEntity> findByBoardCategory(Integer category);

    // 카테고리별 게시글 리스트 및 작성자 정보 포함
    @Query("SELECT b FROM BoardEntity b JOIN FETCH b.user WHERE b.boardCategory = :category")
    List<BoardEntity> findByBoardCategoryWithUser(@Param("category") int category);

    // 모든 게시글 리스트 반환
    List<BoardEntity> findAll();
}
