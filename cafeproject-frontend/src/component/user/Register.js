import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./User.css";

// 회원가입 컴포넌트
function Register() {
  // 아이디, 비밀번호, 비밀번호 확인, 이름, 닉네임 등의 상태 변수 설정
  const [userId, setUserId] = useState(""); // 사용자 아이디
  const [userPassword, setUserPassword] = useState(""); // 비밀번호
  const [rePassword, setRePassword] = useState(""); // 비밀번호 확인
  const [userName, setUserName] = useState(""); // 사용자 이름
  const [userNickname, setUserNickname] = useState(""); // 사용자 닉네임
  const [userIdAvailable, setUserIdAvailable] = useState(null); // 아이디 중복 확인 상태
  const [nicknameAvailable, setNicknameAvailable] = useState(null); // 닉네임 중복 확인 상태

  const navigate = useNavigate(); // 페이지 이동을 위한 훅

  // 아이디와 닉네임 중복 확인 함수
  const checkDuplicate = async (field, value) => {
    try {
      // 중복 확인 API 호출
      const response = await fetch(
        `http://localhost:8080/users/check-duplicate`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ [field]: value }), // 필드와 값 전송
        },
      );

      const result = await response.json();
      return result;
    } catch (error) {
      console.error("중복 확인 에러:", error); // 오류 발생 시 콘솔 출력
      return null;
    }
  };

  // 아이디 중복 확인: userId 상태가 변경될 때 실행
  useEffect(() => {
    if (userId && userId.length >= 4) {
      checkDuplicate("userId", userId).then((result) => {
        setUserIdAvailable(!result?.userIdExists); // 중복 여부 설정
      });
    }
  }, [userId]);

  // 닉네임 중복 확인: userNickname 상태가 변경될 때 실행
  useEffect(() => {
    if (userNickname) {
      checkDuplicate("userNickname", userNickname).then((result) => {
        setNicknameAvailable(!result?.nicknameExists); // 중복 여부 설정
      });
    }
  }, [userNickname]);

  // 회원가입 처리 함수
  const handleSignup = async (e) => {
    e.preventDefault(); // 폼 기본 제출 동작 방지

    // 비밀번호 확인
    if (userPassword !== rePassword) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    // 중복 여부 확인
    if (!userIdAvailable || !nicknameAvailable) {
      alert("아이디 또는 닉네임이 중복되었습니다.");
      return;
    }

    // 아이디 길이 확인
    if (userId.length < 4 || userId.length > 20) {
      alert("아이디는 최소 4자, 최대 20자여야 합니다.");
      return;
    }

    // 회원가입 데이터 객체 생성
    const user = {
      userId,
      userPassword,
      userName,
      userNickname,
    };

    try {
      // 회원가입 API 호출
      const response = await fetch("http://localhost:8080/users/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(user), // 사용자 정보 전송
      });

      if (response.ok) {
        alert("회원가입 성공");
        navigate("/"); // 메인 페이지로 이동
      } else {
        alert("회원가입 실패");
      }
    } catch (error) {
      console.error("회원가입 에러:", error); // 오류 발생 시 콘솔 출력
    }
  };

  // 아이디 중복 확인 버튼 클릭 시 호출
  const handleIdCheck = () => {
    checkDuplicate("userId", userId).then((result) => {
      setUserIdAvailable(!result?.userIdExists);
    });
  };

  // 닉네임 중복 확인 버튼 클릭 시 호출
  const handleNicknameCheck = () => {
    checkDuplicate("userNickname", userNickname).then((result) => {
      setNicknameAvailable(!result?.nicknameExists);
    });
  };

  return (
    <div className="user-page">
      <div className="register-container">
        <h2>회원가입</h2>
        {/* 회원가입 폼 */}
        <form onSubmit={handleSignup}>
          <label>아이디:</label>
          <input
            type="text"
            value={userId}
            onChange={(e) => setUserId(e.target.value)} // 아이디 상태 업데이트
            required
            minLength={4}
            maxLength={20}
          />
          {/* 아이디 중복 여부 표시 */}
          {userIdAvailable === false && (
            <p style={{ color: "red" }}>이미 사용 중인 아이디입니다.</p>
          )}
          {userIdAvailable === true && (
            <p style={{ color: "green" }}>사용 가능한 아이디입니다.</p>
          )}
          {userId.length < 4 && (
            <p style={{ color: "red" }}>아이디는 최소 4자여야 합니다.</p>
          )}
          {userId.length > 20 && (
            <p style={{ color: "red" }}>아이디는 최대 20자까지만 가능합니다.</p>
          )}
          <br />
          <label>비밀번호:</label>
          <input
            type="password"
            value={userPassword}
            onChange={(e) => setUserPassword(e.target.value)} // 비밀번호 상태 업데이트
            required
          />
          <br />
          <label>비밀번호 확인:</label>
          <input
            type="password"
            value={rePassword}
            onChange={(e) => setRePassword(e.target.value)} // 비밀번호 확인 상태 업데이트
            required
          />
          <br />
          <label>이름:</label>
          <input
            type="text"
            value={userName}
            onChange={(e) => setUserName(e.target.value)} // 이름 상태 업데이트
            required
          />
          <br />
          <label>닉네임:</label>
          <input
            type="text"
            value={userNickname}
            onChange={(e) => setUserNickname(e.target.value)} // 닉네임 상태 업데이트
            required
          />
          {/* 닉네임 중복 여부 표시 */}
          {nicknameAvailable === false && (
            <p style={{ color: "red" }}>이미 사용 중인 닉네임입니다.</p>
          )}
          {nicknameAvailable === true && (
            <p style={{ color: "green" }}>사용 가능한 닉네임입니다.</p>
          )}
          <br />
          <button type="submit">회원가입</button> {/* 제출 버튼 */}
        </form>
      </div>
    </div>
  );
}

export default Register;
