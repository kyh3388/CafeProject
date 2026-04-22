package org.cloud.user;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity // 이 클래스가 JPA 엔티티임을 나타냄
@Table(name = "CAFEPROJECT_USER_TB") // 데이터베이스에서 "USER_TB" 테이블과 매핑됨
public class UserEntity {
	
	
	@Column(name = "USER_IMAGE_TYPE")
	private String userImageType;

	public String getUserImageType() {
	    return userImageType;
	}

	public void setUserImageType(String userImageType) {
	    this.userImageType = userImageType;
	}

    @Id // 기본 키 필드
    @Column(name = "USER_ID", nullable = false) // 사용자 아이디 컬럼, 필수
    private String userId;

    @Column(name = "USER_NAME", nullable = false) // 사용자 이름 컬럼, 필수
    private String userName;

    @Column(name = "USER_NICKNAME", nullable = false, unique = true) // 사용자 닉네임 컬럼, 필수 및 유니크 제약 조건
    private String userNickname;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "USER_PW", nullable = false)
    private String userPassword;

    @Lob
    @Column(name = "USER_IMAGE", columnDefinition = "LONGBLOB")
    private byte[] userImage;

    @Column(name = "USER_LV", nullable = false) // 사용자 레벨 컬럼, 필수, 기본값 1
    private Integer userLevel;

    @Column(name = "CRT_DT", nullable = false) // 생성 날짜 컬럼, 필수
    private LocalDateTime createdDate;

    @Column(name = "CRT_USER", nullable = false) // 생성자 컬럼, 필수
    private String createdBy;

    @Column(name = "UDT_DT", nullable = false) // 수정 날짜 컬럼, 필수
    private LocalDateTime updatedDate;

    @Column(name = "UDT_USER", nullable = false) // 수정자 컬럼, 필수
    private String updatedBy;

    
    
    @PrePersist // 엔티티가 처음 저장되기 전에 실행
    protected void onCreate() {
        this.createdDate = LocalDateTime.now(); // 현재 시간으로 생성 날짜 설정
        this.updatedDate = LocalDateTime.now(); // 현재 시간으로 수정 날짜 설정
        if (this.userLevel == null) {
            this.userLevel = 1; // userLevel 기본값 설정
        }
    }

    @PreUpdate // 엔티티가 업데이트되기 전에 실행
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now(); // 현재 시간으로 수정 날짜 갱신
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public byte[] getUserImage() {
        return userImage;
    }

    public void setUserImage(byte[] userImage) {
        this.userImage = userImage;
    }

    public Integer getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(Integer userLevel) {
        this.userLevel = userLevel;
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
