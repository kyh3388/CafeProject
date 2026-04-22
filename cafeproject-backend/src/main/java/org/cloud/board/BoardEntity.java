package org.cloud.board;

import java.time.LocalDateTime;

import org.cloud.user.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity // 이 클래스가 JPA 엔티티임을 나타냄
@Table(name = "CAFEPROJECT_BOARD_TB") // 데이터베이스에서 "BOARD_TB" 테이블과 매핑됨
public class BoardEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "BOARD_NUMBER")
	private Long boardNumber;

    @Column(name = "BOARD_CATEGORY", nullable = false) // 필수 컬럼, 게시물 카테고리
    private Integer boardCategory;

    @Column(name = "BOARD_TITLE", nullable = false) // 필수 컬럼, 게시물 제목
    private String boardTitle;

    @Column(name = "BOARD_WRITE", nullable = false, length = 1000) // 필수 컬럼, 게시물 내용, 최대 길이 1000
    private String boardWrite;

    @ManyToOne // 다대일 관계 설정 (여러 게시글이 하나의 사용자와 연결)
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false) // 외래 키 컬럼 지정
    private UserEntity user;

    @Column(name = "CRT_DT", nullable = false) // 생성 날짜 필수 컬럼
    private LocalDateTime createdDate;

    @Column(name = "CRT_USER", nullable = false) // 생성자 필수 컬럼
    private String createdBy;

    @Column(name = "UDT_DT", nullable = false) // 수정 날짜 필수 컬럼
    private LocalDateTime updatedDate;

    @Column(name = "UDT_USER", nullable = false) // 수정자 필수 컬럼
    private String updatedBy;

    @PrePersist // 엔티티가 처음 저장되기 전에 실행
    protected void onCreate() {
        this.createdDate = LocalDateTime.now(); // 현재 시간으로 생성 날짜 설정
        this.updatedDate = LocalDateTime.now(); // 현재 시간으로 수정 날짜 설정
    }

    @PreUpdate // 엔티티가 업데이트되기 전에 실행
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now(); // 현재 시간으로 수정 날짜 갱신
    }

    // Getters and Setters
    public Long getBoardNumber() {
        return boardNumber;
    }

    public void setBoardNumber(Long boardNumber) {
        this.boardNumber = boardNumber;
    }

    public Integer getBoardCategory() {
        return boardCategory;
    }

    public void setBoardCategory(Integer boardCategory) {
        this.boardCategory = boardCategory;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public void setBoardTitle(String boardTitle) {
        this.boardTitle = boardTitle;
    }

    public String getBoardWrite() {
        return boardWrite;
    }

    public void setBoardWrite(String boardWrite) {
        this.boardWrite = boardWrite;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
