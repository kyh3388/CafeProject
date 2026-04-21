import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./User.css";

// 프로필 수정 컴포넌트
function EditProfile({ user, setUser }) {
  // 사용자 정보와 프로필 수정에 필요한 상태 변수 설정
  const [nickname, setNickname] = useState(user ? user.userNickname : ""); // 닉네임
  const [name, setName] = useState(user ? user.userName : ""); // 이름
  const [userId] = useState(user ? user.userId : ""); // 아이디 (수정 불가)
  const [userLevel] = useState(user ? user.userLevel : 1); // 사용자 레벨 (수정 불가)
  const [password, setPassword] = useState(""); // 비밀번호
  const [confirmPassword, setConfirmPassword] = useState(""); // 비밀번호 확인
  const [profileImage, setProfileImage] = useState(null); // 프로필 이미지

  const navigate = useNavigate(); // 페이지 이동을 위한 훅

  // 프로필 업데이트 처리 함수
  const handleUpdate = async (e) => {
    e.preventDefault(); // 폼 기본 제출 동작 방지

    // 비밀번호 일치 여부 확인
    if (password !== confirmPassword) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    // FormData 객체를 사용하여 이미지와 기타 데이터 전송
    const formData = new FormData();
    formData.append("userNickname", nickname);
    formData.append("userName", name);
    formData.append("userPassword", password);
    formData.append("userLevel", userLevel);

    if (profileImage) {
      formData.append("profileImage", profileImage);
    }

    try {
      // 프로필 업데이트 API 호출
      const response = await fetch(`http://localhost:8080/users/${userId}`, {
        method: "PUT",
        body: formData,
        credentials: "include", // 쿠키 포함
      });

      if (response.ok) {
        const userResponse = await fetch(
          "http://localhost:8080/users/current-user",
          {
            method: "GET",
            credentials: "include",
          },
        );

        if (userResponse.ok) {
          const updatedUser = await userResponse.json();
          setUser(updatedUser);
        }

        alert("프로필이 업데이트되었습니다.");
        navigate("/");
      } else {
        alert("업데이트 실패");
      }
    } catch (error) {
      console.error("업데이트 에러:", error);
    }
  };

  // 이미지 변경 시 호출되는 함수
  const handleImageChange = (e) => {
    setProfileImage(e.target.files[0]); // 선택한 이미지 파일을 상태에 저장
  };

  return (
    <div className="user-page">
      <div className="edit-profile-container">
        <h2>프로필 수정</h2>
        {/* 프로필 수정 폼 */}
        <form onSubmit={handleUpdate}>
          <label>프로필 사진:</label>
          <input
            type="file"
            accept="image/*"
            onChange={handleImageChange} // 이미지 선택 시 handleImageChange 호출
          />
          <br />
          <label>닉네임:</label>
          <input
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)} // 닉네임 상태 업데이트
            required
          />
          <br />
          <label>이름:</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)} // 이름 상태 업데이트
            required
          />
          <br />
          <label>아이디:</label>
          <input
            type="text"
            value={userId}
            disabled // 아이디는 수정 불가
          />
          <br />
          <label>레벨:</label>
          <input
            type="text"
            value={userLevel}
            disabled // 레벨은 수정 불가
          />
          <br />
          <label>비밀번호:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)} // 비밀번호 상태 업데이트
            required
          />
          <br />
          <label>비밀번호 확인:</label>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)} // 비밀번호 확인 상태 업데이트
            required
          />
          <br />
          <button type="submit">수정하기</button> {/* 수정 버튼 */}
        </form>
      </div>
    </div>
  );
}

export default EditProfile;
