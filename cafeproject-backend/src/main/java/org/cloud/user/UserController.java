package org.cloud.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 전체 사용자 목록 가져오기
     */
    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getAllUserDtos();
    }

    /**
     * 사용자 생성 (회원가입 처리)
     */
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UserEntity user) {
        try {
            if (user.getUserId() == null || user.getUserPassword() == null || user.getUserName() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("필수 필드가 누락되었습니다.");
            }

            user.setCreatedBy("System");
            user.setUpdatedBy("System");
            userService.saveUser(user);
            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 로그인 처리
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserEntity user, HttpSession session) {
        UserEntity authenticatedUser = userService.authenticate(user.getUserId(), user.getUserPassword());
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 잘못되었습니다.");
        }

        session.setAttribute("user", authenticatedUser);
        return ResponseEntity.ok("로그인 성공");
    }

    /**
     * 사용자 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공");
    }

    /**
     * 현재 로그인된 사용자 정보 반환
     */
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        return ResponseEntity.ok(userService.toUserDto(user));
    }

    /**
     * 사용자 프로필 업데이트
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(
        @PathVariable String id,
        @RequestParam("userNickname") String nickname,
        @RequestParam("userName") String name,
        @RequestParam("userPassword") String password,
        @RequestParam(value = "userLevel", required = false) Integer userLevel,
        @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
        HttpSession session) {

        try {
            UserEntity loggedInUser = (UserEntity) session.getAttribute("user");
            if (loggedInUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            UserEntity updatedUser = userService.updateUserProfile(id, nickname, name, password, userLevel, profileImage);
            session.setAttribute("user", updatedUser);

            return ResponseEntity.ok("프로필이 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("프로필 업데이트 실패: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable String id, @RequestBody String newPassword, HttpSession session) {
        try {
            UserEntity loggedInUser = (UserEntity) session.getAttribute("user");
            if (loggedInUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            UserEntity updatedUser = userService.updatePassword(id, newPassword);
            session.setAttribute("user", updatedUser);

            return ResponseEntity.ok("비밀번호 업데이트 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * 사용자 삭제
     */
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 닉네임으로 아이디 찾기
     */
    @PostMapping("/find-id")
    public ResponseEntity<?> findIdByNickname(@RequestParam String nickname) {
        try {
            String userId = userService.findUserIdByNickname(nickname);
            if (userId != null) {
                return ResponseEntity.ok("해당 닉네임의 아이디는 " + userId + "입니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 닉네임으로 등록된 아이디가 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("아이디 찾기 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/find-password")
    public ResponseEntity<?> findPasswordByNicknameAndId(@RequestParam String nickname, @RequestParam String userId) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body("비밀번호 찾기 기능은 현재 지원하지 않습니다. 추후 이메일 인증 기반 재설정으로 대체될 예정입니다.");
    }

    /**
     * 관리자에 의한 사용자 정보 업데이트
     */
    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserEntity userDetails) {
        try {
            UserEntity updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(userService.toUserDto(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * 아이디 또는 닉네임 중복 확인
     */
    @PostMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicate(@RequestBody Map<String, String> requestData) {
        String userId = requestData.get("userId");
        String userNickname = requestData.get("userNickname");

        Map<String, Boolean> response = new HashMap<>();
        response.put("userIdExists", userService.isUserIdOrNicknameExists(userId, null));
        response.put("nicknameExists", userService.isUserIdOrNicknameExists(null, userNickname));

        return ResponseEntity.ok(response);
    }
}