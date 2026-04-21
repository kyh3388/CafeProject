import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Board.css";

// 게시물 작성 컴포넌트
function CreatePost() {
  // 제목, 내용, 카테고리를 저장할 상태 변수
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [category, setCategory] = useState(2); // 기본값으로 '자유게시판' 설정
  const navigate = useNavigate(); // 페이지 이동을 위한 훅

  // 폼 제출 처리 함수
  const handleSubmit = async (e) => {
    e.preventDefault(); // 폼 기본 제출 동작 방지

    // 새 게시물 데이터 생성
    const newPost = {
      boardTitle: title,
      boardWrite: content,
      boardCategory: category,
    };

    try {
      // 게시물 작성 API 호출
      const response = await fetch("http://localhost:8080/boards/create", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include", // 쿠키 포함
        body: JSON.stringify(newPost), // JSON 형식으로 데이터 전송
      });

      // 로그인한 사용자만 게시물 작성 가능
      if (!response.ok) {
        console.error(`Error: ${response.status}`); // 오류 상태 콘솔에 출력
        if (response.status === 401) {
          alert("로그인이 필요합니다.");
          navigate("/login"); // 로그인 페이지로 이동
        } else if (response.status === 400) {
          alert("잘못된 요청입니다."); // 유효하지 않은 요청 경고
        } else {
          alert("글 작성 실패");
        }
      } else {
        alert("글 작성이 완료되었습니다.");
        navigate("/"); // 메인 페이지로 이동
      }
    } catch (error) {
      console.error("글 작성 오류:", error); // 오류 콘솔에 출력
    }
  };

  return (
    <div>
      <h2>글쓰기</h2>
      {/* 게시물 작성 폼 */}
      <form onSubmit={handleSubmit}>
        <label>제목:</label>
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)} // 제목 상태 업데이트
          required
        />
        <br />
        <label>내용:</label>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)} // 내용 상태 업데이트
          required
        />
        <br />
        <label>카테고리:</label>
        <select
          value={category}
          onChange={(e) => setCategory(Number(e.target.value))}
        >
          {/* 카테고리 선택 옵션 */}
          <option value={2}>자유게시판</option>
          <option value={3}>질문게시판</option>
          <option value={4}>공지게시판</option>
        </select>
        <br />
        <button type="submit">글 작성</button> {/* 제출 버튼 */}
      </form>
    </div>
  );
}

export default CreatePost;
