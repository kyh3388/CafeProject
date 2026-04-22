import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "./User.css";

// 로그인 컴포넌트
function Login({ setUser }) {
  // 사용자 ID, 비밀번호, 로딩 상태, 에러 메시지 상태 변수
  const [userId, setUserId] = useState(""); // 사용자 ID 상태
  const [password, setPassword] = useState(""); // 비밀번호 상태
  const [loading, setLoading] = useState(false); // 로딩 상태
  const [errorMessage, setErrorMessage] = useState(""); // 에러 메시지 상태
  const navigate = useNavigate(); // 페이지 이동을 위한 훅

  // 로그인 처리 함수
  const handleLogin = async (e) => {
    e.preventDefault(); // 폼 기본 제출 동작 방지

    // 아이디와 비밀번호가 입력되지 않은 경우 에러 메시지 설정
    if (!userId.trim() || !password.trim()) {
      setErrorMessage("아이디와 비밀번호를 입력해주세요.");
      return;
    }

    // 로그인에 필요한 사용자 정보
    const credentials = { userId, userPassword: password };
    setLoading(true); // 로딩 시작
    setErrorMessage(""); // 에러 메시지 초기화

    try {
      // 로그인 API 호출
      const response = await fetch("http://localhost:8080/users/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json", // JSON 형식으로 전송
        },
        body: JSON.stringify(credentials), // 사용자 정보 전송
        credentials: "include", // 쿠키 포함
      });

      if (response.ok) {
        // 로그인 성공 시 사용자 정보 요청 API 호출
        const userResponse = await fetch(
          "http://localhost:8080/users/current-user",
          {
            method: "GET",
            credentials: "include", // 쿠키 포함
          },
        );

        if (userResponse.ok) {
          const user = await userResponse.json(); // 사용자 정보 응답
          setUser(user); // 사용자 상태 업데이트
          alert("로그인 성공");
          navigate("/"); // 메인 페이지로 이동
        } else {
          setErrorMessage("사용자 정보를 가져오는 데 실패했습니다.");
        }
      } else if (response.status === 401) {
        setErrorMessage("아이디 또는 비밀번호가 잘못되었습니다."); // 인증 실패
      } else {
        setErrorMessage("로그인 중 오류가 발생했습니다. 다시 시도해주세요."); // 기타 오류
      }
    } catch (error) {
      console.error("로그인 에러:", error); // 에러 로그 출력
      setErrorMessage("서버와의 연결에 문제가 발생했습니다."); // 서버 연결 문제 메시지
    } finally {
      setLoading(false); // 로딩 상태 종료
    }
  };

  return (
    <div className="user-page">
      <div className="login-container">
        <h2>로그인</h2>
        {/* 에러 메시지 표시 */}
        {errorMessage && <p style={{ color: "red" }}>{errorMessage}</p>}
        {/* 로그인 폼 */}
        <form onSubmit={handleLogin}>
          <label>아이디:</label>
          <input
            type="text"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            required
          />
          <br />

          <label>비밀번호:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <br />

          <button type="submit" disabled={loading}>
            {loading ? "로그인 중..." : "로그인"}{" "}
          </button>
        </form>

        <div style={{ marginTop: "12px", textAlign: "center" }}>
          <Link
            to="/find-id"
            style={{
              fontSize: "13px",
              color: "#666",
              textDecoration: "underline",
            }}
          >
            아이디 찾기
          </Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
