import "./Board.css";
import React, { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

function BoardList({ user }) {
  const { category = "all" } = useParams();
  const [boards, setBoards] = useState([]);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState(category);
  const navigate = useNavigate();

  const pageSize = 10;

  const getCategoryNameInKorean = (categoryId) => {
    switch (categoryId) {
      case "all":
      case 1:
        return "전체";
      case "free":
      case 2:
        return "자유";
      case "questions":
      case 3:
        return "질문";
      case "notice":
      case 4:
        return "공지";
      default:
        return "알 수 없음";
    }
  };

  const formatDateTime = (dateString) => {
    const date = new Date(dateString);

    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();

    return {
      date: `${year}. ${month}. ${day}`,
    };
  };

  useEffect(() => {
    const fetchBoards = async () => {
      try {
        const response = await fetch(
          `http://localhost:8080/boards/category/${category}`,
        );

        if (!response.ok) {
          setError("서버에서 오류가 발생했습니다.");
          return;
        }

        const data = await response.json();
        setBoards(data || []);
        setCurrentPage(0);
      } catch (err) {
        setError("네트워크 오류가 발생했습니다.");
      }
    };

    fetchBoards();
  }, [category]);

  const handleSearch = () => {
    setCurrentPage(0);
    navigate(`/boards/category/${selectedCategory}`);
  };

  const handleCreatePost = () => {
    if (user) {
      navigate("/create-post");
    } else {
      alert("로그인이 필요합니다.");
      navigate("/login");
    }
  };

  const filteredBoards = useMemo(() => {
    const noticeBoards = boards.filter((board) => board.boardCategory === 4);
    const otherBoards = boards.filter((board) => board.boardCategory !== 4);

    otherBoards.sort(
      (a, b) => new Date(b.updatedDate) - new Date(a.updatedDate),
    );

    return [...noticeBoards, ...otherBoards].filter(
      (board) =>
        board.boardTitle.includes(searchTerm) ||
        board.boardWrite.includes(searchTerm),
    );
  }, [boards, searchTerm]);

  const totalPages = Math.max(1, Math.ceil(filteredBoards.length / pageSize));

  const displayedBoards = filteredBoards.slice(
    currentPage * pageSize,
    (currentPage + 1) * pageSize,
  );

  const emptyRows = pageSize - displayedBoards.length;

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage(currentPage + 1);
    }
  };

  const handlePreviousPage = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  if (error) {
    return <div className="board-page">오류: {error}</div>;
  }

  return (
    <div className="board-page board-list-page">
      <div className="board-list-panel">
        <h2>{getCategoryNameInKorean(category)} 게시판</h2>

        <div className="board-controls">
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
          >
            <option value="all">전체</option>
            <option value="notice">공지</option>
            <option value="questions">질문</option>
            <option value="free">자유</option>
          </select>

          <input
            type="text"
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(0);
            }}
            placeholder="검색어를 입력하세요"
          />

          <button onClick={handleSearch}>검색</button>

          <div className="create-post-button-container">
            {user && <button onClick={handleCreatePost}>글쓰기</button>}
          </div>
        </div>

        <div className="board-table-wrap">
          <table className="board-table">
            <thead>
              <tr className="list-up">
                <th className="col-category">카테고리</th>
                <th className="col-title">제목</th>
                <th className="col-content">내용</th>
                <th className="col-date">업데이트 날짜</th>
                <th className="col-writer">작성자</th>
              </tr>
            </thead>
            <tbody>
              {displayedBoards.map((board) => {
                const formatted = formatDateTime(board.updatedDate);

                return (
                  <tr
                    key={board.boardNumber}
                    className={board.boardCategory === 4 ? "notice-row" : ""}
                  >
                    <td>
                      <span
                        className={`category-badge category-${board.boardCategory}`}
                      >
                        {getCategoryNameInKorean(board.boardCategory)}
                      </span>
                    </td>
                    <td className="title-cell">
                      <Link to={`/boards/detail/${board.boardNumber}`}>
                        {board.boardTitle}
                      </Link>
                    </td>
                    <td className="content-cell">
                      <Link to={`/boards/detail/${board.boardNumber}`}>
                        {board.boardWrite}
                      </Link>
                    </td>
                    <td>
                      <div className="date-cell">
                        <span>{formatted.date}</span>
                        <span>{formatted.time}</span>
                      </div>
                    </td>
                    <td>{board.user?.userNickname}</td>
                  </tr>
                );
              })}

              {Array.from({ length: emptyRows }).map((_, index) => (
                <tr key={`empty-${index}`} className="empty-row">
                  <td>&nbsp;</td>
                  <td></td>
                  <td></td>
                  <td></td>
                  <td></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="pagination board-pagination">
          <button onClick={handlePreviousPage} disabled={currentPage === 0}>
            이전 페이지
          </button>
          <span>
            {currentPage + 1} / {totalPages}
          </span>
          <button
            onClick={handleNextPage}
            disabled={currentPage === totalPages - 1}
          >
            다음 페이지
          </button>
        </div>
      </div>
    </div>
  );
}

export default BoardList;
