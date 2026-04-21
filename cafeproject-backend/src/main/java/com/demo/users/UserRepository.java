package com.demo.users;

import org.springframework.data.jpa.repository.JpaRepository;

// UserRepository 인터페이스: JpaRepository를 확장하여 User 엔티티와 데이터베이스 간의 CRUD 작업을 지원
public interface UserRepository extends JpaRepository<UserEntity, String> {
    // 닉네임으로 사용자 찾기 메서드
    UserEntity findByUserNickname(String nickname);

    // 닉네임과 사용자 ID로 사용자 찾기 메서드
    UserEntity findByUserNicknameAndUserId(String nickname, String userId);
}
