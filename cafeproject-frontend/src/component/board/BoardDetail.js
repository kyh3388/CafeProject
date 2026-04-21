import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "./Board.css";

function BoardDetail({ user }) {
  const { boardNumber } = useParams();

  const [board, setBoard] = useState(null);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editedTitle, setEditedTitle] = useState("");
  const [editedContent, setEditedContent] = useState("");
  const navigate = useNavigate();

  const formatDateTime = (dateString) => {
    const date = new Date(dateString);

    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const hour = String(date.getHours()).padStart(2, "0");
    const minute = String(date.getMinutes()).padStart(2, "0");

    return {
      date: `${year}년 ${month}월 ${day}일`,
      time: `${hour}시 ${minute}분`,
    };
  };

  useEffect(() => {
    const fetchBoard = async () => {
      try {
        const response = await fetch(
          `http://localhost:8080/boards/detail/${boardNumber}`,
        );

        if (!response.ok) {
          setError("게시물을 불러오는 중 오류가 발생했습니다.");
        } else {
          const data = await response.json();
          setBoard(data);
          setEditedTitle(data.boardTitle);
          setEditedContent(data.boardWrite);
        }
      } catch (err) {
        setError("네트워크 오류가 발생했습니다.");
      }
    };

    fetchBoard();
  }, [boardNumber]);

  const handleDelete = async () => {
    if (window.confirm("정말로 이 게시물을 삭제하시겠습니까?")) {
      try {
        const response = await fetch(
          `http://localhost:8080/boards/delete/${boardNumber}`,
          {
            method: "DELETE",
          },
        );

        if (response.ok) {
          alert("게시물이 삭제되었습니다.");
          navigate("/");
        } else {
          alert("게시물 삭제에 실패했습니다.");
        }
      } catch (error) {
        alert("네트워크 오류로 게시물 삭제에 실패했습니다.");
      }
    }
  };

  const handleEdit = async () => {
    if (isEditing) {
      try {
        const response = await fetch(
          `http://localhost:8080/boards/update/${boardNumber}`,
          {
            method: "PUT",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              boardTitle: editedTitle,
              boardWrite: editedContent,
            }),
          },
        );

        if (response.ok) {
          const updatedBoard = await response.json();
          setBoard(updatedBoard);
          setIsEditing(false);
          alert("게시물이 수정되었습니다.");
        } else {
          alert("게시물 수정에 실패했습니다.");
        }
      } catch (error) {
        alert("네트워크 오류로 게시물 수정에 실패했습니다.");
      }
    } else {
      setIsEditing(true);
    }
  };

  if (error) {
    return <div className="board-page">오류: {error}</div>;
  }

  if (!board) {
    return <div className="board-page">로딩 중...</div>;
  }

  const userNickname = board.user ? board.user.userNickname : "알 수 없음";
  const userId = board.user ? board.user.userId : null;
  const created = formatDateTime(board.createdDate);

  const isNeverUpdated =
    !board.updatedDate ||
    new Date(board.createdDate).getTime() ===
      new Date(board.updatedDate).getTime();

  const updated = isNeverUpdated ? null : formatDateTime(board.updatedDate);

  return (
    <div className="board-page board-detail-page">
      <div className="board-detail-card">
        <div className="board-detail-header">
          {isEditing ? (
            <input
              className="board-detail-title-input"
              type="text"
              value={editedTitle}
              onChange={(e) => setEditedTitle(e.target.value)}
            />
          ) : (
            <h2 className="board-detail-title">{board.boardTitle}</h2>
          )}
        </div>

        <div className="board-detail-meta">
          <div className="board-detail-meta-item">
            <span className="meta-label">작성자</span>
            <span className="meta-value">{userNickname}</span>
          </div>
          <div className="board-detail-meta-item">
            <span className="meta-label">작성일</span>
            <span className="meta-value">
              {created.date}
              <br />
              {created.time}
            </span>
          </div>
          <div className="board-detail-meta-item">
            <span className="meta-label">수정일</span>
            <span className="meta-value">
              {updated ? (
                <>
                  {updated.date}
                  <br />
                  {updated.time}
                </>
              ) : (
                "-"
              )}
            </span>
          </div>
        </div>

        <div className="board-detail-content-wrap">
          {isEditing ? (
            <textarea
              className="board-detail-textarea"
              value={editedContent}
              onChange={(e) => setEditedContent(e.target.value)}
            />
          ) : (
            <div className="board-detail-content">{board.boardWrite}</div>
          )}
        </div>

        <div className="board-detail-footer">
          <button
            type="button"
            className="board-detail-back-button"
            onClick={() => navigate(-1)}
          >
            뒤로가기
          </button>

          {user && (userId === user.userId || user.userLevel >= 4) && (
            <div className="detail-button-group">
              <button type="button" onClick={handleEdit}>
                {isEditing ? "저장" : "수정"}
              </button>
              <button type="button" onClick={handleDelete}>
                삭제
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default BoardDetail;
