import React, { useState } from "react";
import "./User.css";

// 비밀번호 찾기 컴포넌트
function FindPassword() {
  // 닉네임, 아이디, 메시지를 위한 상태 변수
  const [nickname, setNickname] = useState(""); // 닉네임 입력
  const [userId, setUserId] = useState(""); // 사용자 아이디 입력
  const [message, setMessage] = useState(""); // 결과 메시지

  // 비밀번호 찾기 요청 함수
  const handleFindPassword = async (e) => {
    e.preventDefault(); // 폼 기본 제출 동작 방지
    try {
      // 비밀번호 찾기 API 호출
      const response = await fetch(
        "http://localhost:8080/users/find-password",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded", // URL 인코딩 방식으로 전송
          },
          body: new URLSearchParams({ nickname, userId }), // 닉네임과 아이디 데이터 전송
        },
      );
      const result = await response.text(); // 서버 응답 메시지
      setMessage(result); // 메시지 상태 업데이트
    } catch (error) {
      setMessage("비밀번호 찾기 중 오류 발생."); // 오류 발생 시 메시지 설정
    }
  };

  return (
    <div className="user-page">
      <div className="find-container">
        <h2>비밀번호 찾기</h2>
        {/* 비밀번호 찾기 폼 */}
        <form onSubmit={handleFindPassword}>
          <label>닉네임:</label>
          <input
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)} // 닉네임 상태 업데이트
            required
          />
          <label>아이디:</label>
          <input
            type="text"
            value={userId}
            onChange={(e) => setUserId(e.target.value)} // 아이디 상태 업데이트
            required
          />
          <button type="submit">비밀번호 찾기</button> {/* 제출 버튼 */}
        </form>
        {/* 결과 메시지 표시 */}
        {message && <p>{message}</p>}
      </div>
    </div>
  );
}

export default FindPassword;
