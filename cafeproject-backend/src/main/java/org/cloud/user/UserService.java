package org.cloud.user;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 모든 사용자 조회
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    // 모든 사용자 DTO 조회
    public List<UserDto> getAllUserDtos() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserDto)
                .toList();
    }

    // Entity -> DTO 변환
    public UserDto toUserDto(UserEntity user) {
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

    // 사용자 저장(회원가입)
    @Transactional
    public UserEntity saveUser(UserEntity user) {
        try {
            user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
            return userRepository.save(user);
        } catch (Exception e) {
            System.out.println("사용자 저장 오류: " + e.getMessage());
            throw new RuntimeException("사용자 저장 중 오류 발생", e);
        }
    }

    // 사용자 ID로 사용자 정보 가져오기
    public UserEntity getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    // 사용자 인증
    public UserEntity authenticate(String userId, String userPassword) {
        if (userId == null || userPassword == null || userPassword.isBlank()) {
            return null;
        }

        UserEntity user = getUserById(userId);

        if (user != null && passwordEncoder.matches(userPassword, user.getUserPassword())) {
            return user;
        }
        return null;
    }

    // 사용자 프로필 업데이트
    @Transactional
    public UserEntity updateUserProfile(String id, String nickname, String name, String password,
                                        Integer userLevel, MultipartFile profileImage) {
        Optional<UserEntity> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();

            user.setUserNickname(nickname);
            user.setUserName(name);

            if (password != null && !password.isBlank()) {
                user.setUserPassword(passwordEncoder.encode(password));
            }

            if (userLevel != null) {
                user.setUserLevel(userLevel);
            }

            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    user.setUserImage(profileImage.getBytes());
                    user.setUserImageType(profileImage.getContentType());
                } catch (IOException e) {
                    throw new RuntimeException("프로필 이미지 처리 중 오류 발생", e);
                }
            }

            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with id " + id);
        }
    }

    // 비밀번호 업데이트
    @Transactional
    public UserEntity updatePassword(String id, String newPassword) {
        Optional<UserEntity> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();

            String password = newPassword == null ? null : newPassword.trim();

            if (password == null || password.isBlank()) {
                throw new RuntimeException("새 비밀번호가 비어 있습니다.");
            }

            user.setUserPassword(passwordEncoder.encode(password));
            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with id " + id);
        }
    }

    // 닉네임으로 사용자 ID 찾기
    public String findUserIdByNickname(String nickname) {
        UserEntity user = userRepository.findByUserNickname(nickname);
        return user != null ? user.getUserId() : null;
    }

    // 사용자 삭제
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    // 사용자 정보 업데이트 (관리자 전용)
    @Transactional
    public UserEntity updateUser(String id, UserEntity userDetails) {
        Optional<UserEntity> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();

            user.setUserNickname(userDetails.getUserNickname());
            user.setUserName(userDetails.getUserName());
            user.setUserLevel(userDetails.getUserLevel());

            if (userDetails.getUserPassword() != null
                    && !userDetails.getUserPassword().isBlank()
                    && !userDetails.getUserPassword().equals(user.getUserPassword())) {
                user.setUserPassword(passwordEncoder.encode(userDetails.getUserPassword()));
            }

            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with id " + id);
        }
    }

    // 아이디 또는 닉네임 중복 확인
    public boolean isUserIdOrNicknameExists(String userId, String userNickname) {
        boolean userIdExists = false;
        boolean nicknameExists = false;

        if (userId != null && !userId.isEmpty()) {
            userIdExists = userRepository.findById(userId).isPresent();
        }

        if (userNickname != null && !userNickname.isEmpty()) {
            nicknameExists = userRepository.findByUserNickname(userNickname) != null;
        }

        return userIdExists || nicknameExists;
    }
}