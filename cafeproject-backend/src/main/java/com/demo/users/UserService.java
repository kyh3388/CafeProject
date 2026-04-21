package com.demo.users; // 이 클래스가 속한 패키지를 정의

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service; // 서비스 레이어를 나타내는 Spring 애너테이션
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 관리를 위한 애너테이션
import org.springframework.web.multipart.MultipartFile; // 파일 업로드 처리를 위한 클래스

/**
 * UserService 클래스
 * 사용자 데이터 처리 및 비즈니스 로직을 구현하는 서비스 클래스입니다.
 * 주요 기능:
 * - 사용자 CRUD 작업
 * - 인증 및 사용자 검색
 * - 비밀번호 및 프로필 업데이트
 */
@Service // 이 클래스가 Spring 서비스 컴포넌트임을 나타냄
public class UserService {
    private final UserRepository userRepository; // 사용자 데이터베이스 접근을 위한 리포지토리

    /**
     * UserService 생성자
     * @param userRepository 사용자 데이터 처리를 위한 리포지토리
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 모든 사용자 조회
     * @return 사용자 리스트
     */
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll(); // 데이터베이스에서 모든 사용자 정보를 조회
    }

    /**
     * 사용자 저장
     * @param user 저장할 사용자 정보
     * @return 저장된 사용자 정보
     */
    @Transactional // 데이터 저장 중 트랜잭션 관리로 일관성 보장
    public UserEntity saveUser(UserEntity user) {
        try {
            return userRepository.save(user); // 사용자 데이터를 저장
        } catch (Exception e) {
            System.out.println("사용자 저장 오류: " + e.getMessage());
            throw new RuntimeException("사용자 저장 중 오류 발생", e); // 오류 발생 시 예외 처리
        }
    }

    /**
     * 사용자 ID로 사용자 정보 가져오기
     * @param userId 사용자 ID
     * @return 사용자 정보 또는 null
     */
    public UserEntity getUserById(String userId) {
        return userRepository.findById(userId).orElse(null); // 사용자 ID로 데이터 조회
    }

    /**
     * 사용자 인증
     * @param userId 사용자 ID
     * @param userPassword 사용자 비밀번호
     * @return 인증된 사용자 정보 또는 null
     */
    public UserEntity authenticate(String userId, String userPassword) {
        UserEntity user = getUserById(userId); // 사용자 정보 가져오기
        if (user != null && user.getUserPassword().equals(userPassword)) {
            return user; // 비밀번호가 일치하면 사용자 반환
        }
        return null; // 인증 실패 시 null 반환
    }

    /**
     * 사용자 프로필 업데이트
     * @param id 사용자 ID
     * @param nickname 새 닉네임
     * @param name 새 이름
     * @param password 새 비밀번호
     * @param userLevel 새 사용자 레벨
     * @param profileImage 새 프로필 이미지
     */
    public void updateUserProfile(String id, String nickname, String name, String password, Integer userLevel, MultipartFile profileImage) {
        Optional<UserEntity> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            user.setUserNickname(nickname); // 닉네임 업데이트
            user.setUserName(name); // 이름 업데이트
            user.setUserPassword(password); // 비밀번호 업데이트
            user.setUserLevel(userLevel); // 사용자 레벨 업데이트

            // 프로필 이미지 처리
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    user.setUserImage(profileImage.getBytes());
                    user.setUserImageType(profileImage.getContentType());
                } catch (IOException e) {
                    throw new RuntimeException("프로필 이미지 처리 중 오류 발생", e);
                }
            }

            userRepository.save(user); // 업데이트된 사용자 저장
        } else {
            throw new RuntimeException("User not found with id " + id); // 사용자 미존재 시 예외 처리
        }
    }

    /**
     * 비밀번호 업데이트
     * @param id 사용자 ID
     * @param newPassword 새 비밀번호
     */
    public void updatePassword(String id, String newPassword) {
        Optional<UserEntity> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            user.setUserPassword(newPassword); // 새 비밀번호 설정
            userRepository.save(user); // 변경 사항 저장
        } else {
            throw new RuntimeException("User not found with id " + id); // 사용자 미존재 시 예외 처리
        }
    }

    /**
     * 닉네임으로 사용자 ID 찾기
     * @param nickname 사용자 닉네임
     * @return 사용자 ID 또는 null
     */
    public String findUserIdByNickname(String nickname) {
        UserEntity user = userRepository.findByUserNickname(nickname); // 닉네임으로 사용자 조회
        return user != null ? user.getUserId() : null; // ID 반환 또는 null
    }

    /**
     * 닉네임과 ID로 비밀번호 찾기
     * @param nickname 사용자 닉네임
     * @param userId 사용자 ID
     * @return 비밀번호 또는 null
     */
    public String findUserPasswordByNicknameAndId(String nickname, String userId) {
        UserEntity user = userRepository.findByUserNicknameAndUserId(nickname, userId); // 닉네임과 ID로 사용자 조회
        return user != null ? user.getUserPassword() : null; // 비밀번호 반환 또는 null
    }

    /**
     * 사용자 삭제
     * @param userId 삭제할 사용자 ID
     */
    public void deleteUser(String userId) {
        userRepository.deleteById(userId); // 사용자 삭제
    }

    /**
     * 사용자 정보 업데이트 (관리자 전용)
     * @param id 사용자 ID
     * @param userDetails 업데이트할 사용자 정보
     * @return 업데이트된 사용자 정보
     */
    @Transactional
    public UserEntity updateUser(String id, UserEntity userDetails) {
        Optional<UserEntity> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            user.setUserNickname(userDetails.getUserNickname()); // 닉네임 설정
            user.setUserName(userDetails.getUserName()); // 이름 설정
            user.setUserLevel(userDetails.getUserLevel()); // 사용자 레벨 설정
            user.setUserPassword(userDetails.getUserPassword()); // 비밀번호 설정
            return userRepository.save(user); // 업데이트된 사용자 저장
        } else {
            throw new RuntimeException("User not found with id " + id); // 사용자 미존재 시 예외 처리
        }
    }

    /**
     * 아이디 또는 닉네임 중복 확인
     * @param userId 사용자 ID
     * @param userNickname 사용자 닉네임
     * @return 중복 여부 (true/false)
     */
    public boolean isUserIdOrNicknameExists(String userId, String userNickname) {
        boolean userIdExists = false;
        boolean nicknameExists = false;

        // 아이디 중복 여부 확인
        if (userId != null && !userId.isEmpty()) {
            userIdExists = userRepository.findById(userId).isPresent();
        }

        // 닉네임 중복 여부 확인
        if (userNickname != null && !userNickname.isEmpty()) {
            nicknameExists = userRepository.findByUserNickname(userNickname) != null;
        }

        return userIdExists || nicknameExists; // 중복 여부 반환
    }
}
