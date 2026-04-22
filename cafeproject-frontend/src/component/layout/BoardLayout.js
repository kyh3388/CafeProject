import React from "react";
import { Link, Outlet, useNavigate } from "react-router-dom";
import "./Layout.css";

function BoardLayout({ user, setUser, postCount, error }) {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      const response = await fetch("http://localhost:8080/users/logout", {
        method: "POST",
        credentials: "include",
      });

      if (response.ok) {
        localStorage.removeItem("token");
        setUser(null);
        alert("로그아웃 되었습니다.");
        navigate("/login");
      } else {
        throw new Error("로그아웃 실패");
      }
    } catch (error) {
      console.error("로그아웃 에러:", error);
    }
  };

  return (
    <div className="container">
      <header>
        <Link to="/" className="banner">
          메인 배너
        </Link>
      </header>

      <aside className="aside-container">
        <div className="aside-section">
          {error ? (
            <div>오류 발생: {error}</div>
          ) : !user ? (
            <div className="login-section">
              <ul className="login-menu">
                <Link to="/login">로그인</Link>
                <Link to="/register">회원가입</Link>
              </ul>
            </div>
          ) : (
            <div className="user-info">
              {user.userImage && (
                <img
                  src={`data:${user.userImageType || "image/jpeg"};base64,${user.userImage}`}
                  alt="프로필 이미지"
                  style={{
                    width: "100px",
                    height: "100px",
                    borderRadius: "50%",
                  }}
                />
              )}
              <p>닉네임 : {user.userNickname}</p>
              <p>아이디 : {user.userId}</p>
              <p>레벨 : {user.userLevel}</p>
              <Link to="/my-posts">내가 쓴 글</Link> {postCount} <br />
              <Link to="/edit-profile">개인정보수정</Link>
              {user.userLevel === 4 && (
                <Link to="/user-management">사용자관리</Link>
              )}
              <button onClick={handleLogout} className="logout-button">
                로그아웃
              </button>
            </div>
          )}
        </div>

        <div className="aside-section">
          <ul className="category">
            <li>카테고리</li>
            <hr width="100%" color="black" />
            <Link to="/boards/category/all">전체게시판</Link>

            <Link to="/boards/category/notice">공지게시판</Link>

            <Link to="/boards/category/free">자유게시판</Link>

            <Link to="/boards/category/questions">질문게시판</Link>
          </ul>
        </div>
      </aside>

      <main>
        <Outlet />
      </main>
    </div>
  );
}

export default BoardLayout;
