import React, { useEffect, useState } from "react";
import { Routes, Route } from "react-router-dom";
import "./App.css";

import BoardLayout from "./component/layout/BoardLayout";

import BoardDetail from "./component/board/BoardDetail";
import BoardList from "./component/board/BoardList";
import BoardWrite from "./component/board/BoardWrite";
import MyPosts from "./component/board/MyPosts";

import EditProfile from "./component/user/EditProfile";
import FindId from "./component/user/FindId";
import Login from "./component/user/Login";
import Register from "./component/user/Register";

import UserManagement from "./component/admin/UserManagement";

function App() {
  const [user, setUser] = useState(null);
  const [postCount, setPostCount] = useState(0);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        const response = await fetch(
          "http://localhost:8080/users/current-user",
          {
            method: "GET",
            credentials: "include",
          },
        );

        if (response.ok) {
          const loginUser = await response.json();
          setUser(loginUser);
        } else if (response.status === 401) {
          setUser(null);
        } else {
          throw new Error("서버 응답에 문제가 있습니다.");
        }
      } catch (error) {
        console.error("사용자 정보를 가져오는 중 오류 발생:", error);
        setUser(null);
      }
    };

    fetchCurrentUser();
  }, []);

  useEffect(() => {
    const fetchPostCount = async () => {
      if (!user) return;

      try {
        const response = await fetch(
          `http://localhost:8080/boards/count/${user.userId}`,
          {
            credentials: "include",
          },
        );

        if (!response.ok) {
          throw new Error("게시글 개수를 불러오는 중 오류 발생");
        }

        const data = await response.json();
        setPostCount(data);
      } catch (error) {
        console.error("게시글 개수 가져오기 실패:", error);
        setError(error.message);
      }
    };

    fetchPostCount();
  }, [user]);

  return (
    <Routes>
      <Route
        path="/"
        element={
          <BoardLayout
            user={user}
            setUser={setUser}
            postCount={postCount}
            error={error}
          />
        }
      >
        <Route index element={<BoardList user={user} />} />
        <Route
          path="boards/category/:category"
          element={<BoardList user={user} />}
        />
        <Route path="register" element={<Register />} />
        <Route path="login" element={<Login setUser={setUser} />} />
        <Route path="find-id" element={<FindId />} />
        <Route
          path="boards/detail/:boardNumber"
          element={<BoardDetail user={user} />}
        />

        {user && (
          <>
            <Route path="my-posts" element={<MyPosts user={user} />} />
            <Route
              path="edit-profile"
              element={<EditProfile user={user} setUser={setUser} />}
            />
            <Route path="create-post" element={<BoardWrite user={user} />} />
            {user.userLevel === 4 && (
              <Route path="user-management" element={<UserManagement />} />
            )}
          </>
        )}
      </Route>
    </Routes>
  );
}

export default App;
