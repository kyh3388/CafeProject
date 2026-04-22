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

  const [comments, setComments] = useState([]);
  const [commentPage, setCommentPage] = useState(0);
  const [commentTotalPages, setCommentTotalPages] = useState(1);
  const [rootCommentContent, setRootCommentContent] = useState("");
  const [replyParentId, setReplyParentId] = useState(null);
  const [replyContent, setReplyContent] = useState("");

  const navigate = useNavigate();
  const commentPageSize = 10;

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

  const fetchComments = async (page = commentPage) => {
    try {
      const response = await fetch(
        `http://localhost:8080/boards/${boardNumber}/comments?page=${page}&size=${commentPageSize}`,
        {
          credentials: "include",
        },
      );

      if (!response.ok) {
        throw new Error("댓글을 불러오지 못했습니다.");
      }

      const data = await response.json();
      setComments(data.comments || []);
      setCommentPage(data.currentPage ?? 0);
      setCommentTotalPages(Math.max(1, data.totalPages ?? 1));
    } catch (err) {
      console.error("댓글 조회 오류:", err);
    }
  };

  useEffect(() => {
    fetchBoard();
  }, [boardNumber]);

  useEffect(() => {
    fetchComments(commentPage);
  }, [boardNumber, commentPage]);

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
      } catch (deleteError) {
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
      } catch (editError) {
        alert("네트워크 오류로 게시물 수정에 실패했습니다.");
      }
    } else {
      setIsEditing(true);
    }
  };

  const handleCreateRootComment = async () => {
    if (!user) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }

    if (!rootCommentContent.trim()) {
      alert("댓글 내용을 입력해주세요.");
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/boards/${boardNumber}/comments`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({
            commentContent: rootCommentContent,
            parentCommentId: null,
          }),
        },
      );

      if (!response.ok) {
        const message = await response.text();
        alert(message || "댓글 작성에 실패했습니다.");
        return;
      }

      setRootCommentContent("");
      setCommentPage(0);
      await fetchComments(0);
    } catch (commentError) {
      alert("네트워크 오류로 댓글 작성에 실패했습니다.");
    }
  };

  const handleCreateReply = async () => {
    if (!user) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }

    if (!replyParentId) {
      return;
    }

    if (!replyContent.trim()) {
      alert("답글 내용을 입력해주세요.");
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/boards/${boardNumber}/comments`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({
            commentContent: replyContent,
            parentCommentId: replyParentId,
          }),
        },
      );

      if (!response.ok) {
        const message = await response.text();
        alert(message || "답글 작성에 실패했습니다.");
        return;
      }

      setReplyParentId(null);
      setReplyContent("");
      await fetchComments(commentPage);
    } catch (replyError) {
      alert("네트워크 오류로 답글 작성에 실패했습니다.");
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm("이 댓글을 삭제하시겠습니까?")) {
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/comments/${commentId}`,
        {
          method: "DELETE",
          credentials: "include",
        },
      );

      if (!response.ok) {
        const message = await response.text();
        alert(message || "댓글 삭제에 실패했습니다.");
        return;
      }

      await fetchComments(commentPage);
    } catch (deleteError) {
      alert("네트워크 오류로 댓글 삭제에 실패했습니다.");
    }
  };

  const canDeleteComment = (comment) => {
    if (!user) return false;
    return comment.userId === user.userId || user.userLevel >= 4;
  };

  const renderReplyEditor = (parentId) => {
    if (replyParentId !== parentId) return null;

    return (
      <div className="comment-reply-editor">
        <textarea
          value={replyContent}
          onChange={(e) => setReplyContent(e.target.value)}
          placeholder="답글을 입력하세요"
        />
        <div className="comment-editor-buttons">
          <button type="button" onClick={handleCreateReply}>
            등록
          </button>
          <button
            type="button"
            className="comment-cancel-button"
            onClick={() => {
              setReplyParentId(null);
              setReplyContent("");
            }}
          >
            취소
          </button>
        </div>
      </div>
    );
  };

  const renderCommentItem = (comment, depth = 0) => {
    const created = formatDateTime(comment.createdDate);
    const isDeleted = comment.deletedYn === "Y";
    const canReply = user && depth < 2 && !isDeleted;

    return (
      <div
        key={comment.commentId}
        className={`comment-item depth-${depth} ${isDeleted ? "deleted-comment" : ""}`}
      >
        <div className="comment-header">
          <div className="comment-author-block">
            <span className="comment-author">{comment.userNickname}</span>
            <span className="comment-date">
              {created.date} {created.time}
            </span>
          </div>

          <div className="comment-actions">
            {canReply && (
              <button
                type="button"
                onClick={() => {
                  setReplyParentId(comment.commentId);
                  setReplyContent("");
                }}
              >
                답글
              </button>
            )}

            {!isDeleted && canDeleteComment(comment) && (
              <button
                type="button"
                className="comment-delete-button"
                onClick={() => handleDeleteComment(comment.commentId)}
              >
                삭제
              </button>
            )}
          </div>
        </div>

        <div className="comment-body">{comment.commentContent}</div>

        {renderReplyEditor(comment.commentId)}

        {comment.children && comment.children.length > 0 && (
          <div className="comment-children">
            {comment.children.map((child) =>
              renderCommentItem(child, depth + 1),
            )}
          </div>
        )}
      </div>
    );
  };

  if (error) {
    return <div className="board-page">오류: {error}</div>;
  }

  if (!board) {
    return <div className="board-page">로딩 중...</div>;
  }

  const boardUserNickname = board.user ? board.user.userNickname : "알 수 없음";
  const boardUserId = board.user ? board.user.userId : null;
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
            <span className="meta-value">{boardUserNickname}</span>
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

          {user && (boardUserId === user.userId || user.userLevel >= 4) && (
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

        <div className="comment-section">
          <div className="comment-section-header">
            <h3>댓글</h3>
            <p>대대댓글까지만 작성할 수 있습니다.</p>
          </div>

          <div className="comment-editor">
            <textarea
              value={rootCommentContent}
              onChange={(e) => setRootCommentContent(e.target.value)}
              placeholder={
                user
                  ? "댓글을 입력하세요"
                  : "로그인 후 댓글을 작성할 수 있습니다"
              }
              disabled={!user}
            />
            <div className="comment-editor-buttons">
              <button
                type="button"
                onClick={handleCreateRootComment}
                disabled={!user}
              >
                댓글 등록
              </button>
            </div>
          </div>

          <div className="comment-list">
            {comments.length > 0 ? (
              comments.map((comment) => renderCommentItem(comment, 0))
            ) : (
              <div className="comment-empty">첫 댓글을 남겨보세요.</div>
            )}
          </div>

          <div className="comment-pagination">
            <button
              type="button"
              onClick={() => setCommentPage((prev) => prev - 1)}
              disabled={commentPage === 0}
            >
              이전 댓글
            </button>
            <span>
              {commentPage + 1} / {commentTotalPages}
            </span>
            <button
              type="button"
              onClick={() => setCommentPage((prev) => prev + 1)}
              disabled={commentPage >= commentTotalPages - 1}
            >
              다음 댓글
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default BoardDetail;
