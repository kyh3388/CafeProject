import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Board.css";

function CreatePost({ user }) {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [category, setCategory] = useState(2);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!user) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }

    if (category === 4 && user.userLevel < 4) {
      alert("공지글은 관리자(LV4)만 작성할 수 있습니다.");
      return;
    }

    const newPost = {
      boardTitle: title,
      boardWrite: content,
      boardCategory: category,
    };

    try {
      const response = await fetch("http://localhost:8080/boards/create", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify(newPost),
      });

      if (!response.ok) {
        console.error(`Error: ${response.status}`);
        if (response.status === 401) {
          alert("로그인이 필요합니다.");
          navigate("/login");
        } else if (response.status === 400) {
          alert("잘못된 요청입니다.");
        } else {
          alert("글 작성 실패");
        }
      } else {
        alert("글 작성이 완료되었습니다.");
        navigate("/");
      }
    } catch (error) {
      console.error("글 작성 오류:", error);
    }
  };

  return (
    <div className="board-page board-write-page">
      <div className="board-write-card">
        <div className="board-write-header">
          <h2 className="board-write-title">글쓰기</h2>
          <p className="board-write-subtitle">
            게시판 성격에 맞는 카테고리를 선택하고 내용을 작성해주세요.
          </p>
        </div>

        <form className="board-write-form" onSubmit={handleSubmit}>
          <div className="board-write-field">
            <label htmlFor="board-title">제목</label>
            <input
              id="board-title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="제목을 입력하세요"
              maxLength={255}
              required
            />
          </div>

          <div className="board-write-field">
            <label htmlFor="board-content">내용</label>
            <textarea
              id="board-content"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="내용을 입력하세요"
              required
            />
          </div>

          <div className="board-write-bottom-row">
            <div className="board-write-category">
              <label htmlFor="board-category">카테고리</label>
              <select
                value={category}
                onChange={(e) => setCategory(Number(e.target.value))}
              >
                <option value={2}>자유게시판</option>
                <option value={3}>질문게시판</option>
                <option value={4} disabled={!user || user.userLevel < 4}>
                  공지게시판{" "}
                  {!user || user.userLevel < 4 ? "(관리자 전용)" : ""}
                </option>
              </select>
            </div>

            <div className="board-write-button-group">
              <button
                type="button"
                className="board-write-cancel-button"
                onClick={() => navigate(-1)}
              >
                취소
              </button>
              <button type="submit" className="board-write-submit-button">
                글 작성
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreatePost;
