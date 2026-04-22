import React, { useEffect, useState } from "react";
import "./Admin.css";

// 사용자 관리 컴포넌트
function UserManagement() {
  const [users, setUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [editUser, setEditUser] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const usersPerPage = 10;

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await fetch("http://localhost:8080/users");
        const data = await response.json();
        setUsers(data);
      } catch (error) {
        console.error("사용자 목록을 불러오는 중 오류 발생:", error);
      }
    };
    fetchUsers();
  }, []);

  const handleDeleteUser = async (userId) => {
    const targetUser = users.find((user) => user.userId === userId);

    if (!targetUser) {
      return;
    }

    if (targetUser.userLevel === 4) {
      alert("LV4 사용자는 삭제할 수 없습니다.");
      return;
    }

    const confirmed = window.confirm(
      `${targetUser.userId} 사용자를 정말 삭제하시겠습니까?`,
    );

    if (!confirmed) {
      return;
    }

    try {
      await fetch(`http://localhost:8080/users/delete/${userId}`, {
        method: "DELETE",
      });

      setUsers(users.filter((user) => user.userId !== userId));
    } catch (error) {
      console.error("사용자 삭제 중 오류 발생:", error);
    }
  };

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
    setCurrentPage(1);
  };

  const handleEditUser = (user) => {
    setEditUser({ ...user });
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setEditUser({
      ...editUser,
      [name]: value,
    });
  };

  const handleUpdateUser = async (e) => {
    e.preventDefault();

    const originalUser = users.find((user) => user.userId === editUser.userId);

    if (editUser.userLevel < 1 || editUser.userLevel > 4) {
      alert("레벨은 1~4까지 가능.");
      return;
    }

    if (
      originalUser &&
      originalUser.userLevel === 4 &&
      Number(editUser.userLevel) !== 4
    ) {
      alert("LV4 사용자의 레벨은 수정할 수 없습니다.");
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/users/admin/${editUser.userId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(editUser),
        },
      );

      if (response.ok) {
        setUsers(
          users.map((user) =>
            user.userId === editUser.userId ? editUser : user,
          ),
        );
        setEditUser(null);
      } else {
        throw new Error("사용자 정보 업데이트 실패");
      }
    } catch (error) {
      console.error("사용자 업데이트 중 오류 발생:", error);
    }
  };

  const indexOfLastUser = currentPage * usersPerPage;
  const indexOfFirstUser = indexOfLastUser - usersPerPage;

  const filteredUsers = users.filter(
    (user) =>
      user.userNickname.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.userId.toLowerCase().includes(searchTerm.toLowerCase()),
  );

  const currentUsers = filteredUsers.slice(indexOfFirstUser, indexOfLastUser);
  const totalPages = Math.max(
    1,
    Math.ceil(filteredUsers.length / usersPerPage),
  );

  const handleNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  const handlePreviousPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  const renderTable = () => {
    return (
      <div className="admin-card">
        <div className="admin-toolbar">
          <div>
            <h2 className="admin-title">사용자 관리</h2>
            <p className="admin-subtitle">
              사용자 정보를 조회하고 닉네임, 이름, 레벨을 관리합니다.
            </p>
          </div>

          <div className="admin-search-wrap">
            <input
              className="admin-search-input"
              type="text"
              placeholder="닉네임, 이름 또는 아이디 검색"
              value={searchTerm}
              onChange={handleSearch}
            />
          </div>
        </div>

        <div className="admin-summary">
          <div className="admin-summary-item">
            <span className="summary-label">전체 사용자</span>
            <strong className="summary-value">{users.length}</strong>
          </div>
          <div className="admin-summary-item">
            <span className="summary-label">검색 결과</span>
            <strong className="summary-value">{filteredUsers.length}</strong>
          </div>
          <div className="admin-summary-item">
            <span className="summary-label">현재 페이지</span>
            <strong className="summary-value">
              {currentPage} / {totalPages}
            </strong>
          </div>
        </div>

        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>프로필 사진</th>
                <th>닉네임</th>
                <th>이름</th>
                <th>아이디</th>
                <th>레벨</th>
                <th>수정</th>
                <th>삭제</th>
              </tr>
            </thead>
            <tbody>
              {currentUsers.length > 0 ? (
                currentUsers.map((user) => (
                  <tr key={user.userId}>
                    <td>
                      {user.userImage ? (
                        <img
                          className="admin-profile-image"
                          src={`data:${user.userImageType || "image/jpeg"};base64,${user.userImage}`}
                          alt="프로필 이미지"
                        />
                      ) : (
                        <div className="admin-profile-placeholder">
                          {user.userNickname?.charAt(0) || "U"}
                        </div>
                      )}
                    </td>
                    <td>{user.userNickname}</td>
                    <td>{user.userName}</td>
                    <td>{user.userId}</td>
                    <td>
                      <span
                        className={`admin-level-badge level-${user.userLevel}`}
                      >
                        Lv.{user.userLevel}
                      </span>
                    </td>
                    <td>
                      <button
                        className="admin-btn admin-btn-edit"
                        onClick={() => handleEditUser(user)}
                      >
                        수정
                      </button>
                    </td>
                    <td>
                      <button
                        className="admin-btn admin-btn-delete"
                        onClick={() => handleDeleteUser(user.userId)}
                      >
                        삭제
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" className="admin-empty-message">
                    검색 결과가 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  const renderEditForm = () => {
    return (
      <div className="admin-edit-card">
        <h3 className="admin-edit-title">사용자 정보 수정</h3>

        <form className="admin-edit-form" onSubmit={handleUpdateUser}>
          <label>
            닉네임
            <input
              type="text"
              name="userNickname"
              value={editUser.userNickname}
              onChange={handleChange}
            />
          </label>

          <label>
            이름
            <input
              type="text"
              name="userName"
              value={editUser.userName}
              onChange={handleChange}
            />
          </label>

          <label>
            아이디
            <input type="text" name="userId" value={editUser.userId} disabled />
          </label>

          <label>
            레벨 (1~4)
            <input
              type="number"
              name="userLevel"
              value={editUser.userLevel}
              onChange={handleChange}
              min="1"
              max="4"
              disabled={Number(editUser.userLevel) === 4}
            />
          </label>

          <div className="admin-edit-button-group">
            <button type="submit" className="admin-btn admin-btn-save">
              저장
            </button>
            <button
              type="button"
              className="admin-btn admin-btn-cancel"
              onClick={() => setEditUser(null)}
            >
              취소
            </button>
          </div>
        </form>
      </div>
    );
  };

  return (
    <div className="admin-page">
      {editUser ? renderEditForm() : renderTable()}

      {!editUser && (
        <div className="admin-pagination">
          <button onClick={handlePreviousPage} disabled={currentPage === 1}>
            이전
          </button>

          {Array.from({ length: totalPages }, (_, index) => (
            <button
              key={index + 1}
              className={currentPage === index + 1 ? "active-page" : ""}
              onClick={() => setCurrentPage(index + 1)}
              disabled={currentPage === index + 1}
            >
              {index + 1}
            </button>
          ))}

          <button
            onClick={handleNextPage}
            disabled={currentPage === totalPages}
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}

export default UserManagement;
