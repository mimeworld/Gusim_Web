import React from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import "./styles.css";
import { Provider } from "./context";
import { Header, Protected } from "./components";
import EditPost from "./pages/EditPost";
import PostDetail from "./pages/PostDetail";
import ComposePost from "./pages/ComposePost";
import ChangePassword from "./pages/auth/ChangePassword";
import MessageDialog from "./components/MessageDialog";
import Home from "./pages/Home";
import About from "./pages/About";
import Activities from "./pages/Activities";
import Share from "./pages/Share";
import UploadPhoto from "./pages/UploadPhoto";
import MyPhotos from "./pages/MyPhotos";
import Members from "./pages/Members";
import MemberProfile from "./pages/MemberProfile";
import MyPage from "./pages/MyPage";
import Equipment from "./pages/Equipment";
import Login from "./pages/auth/Login";
import Officers from "./pages/Officers";
import Board from "./pages/Board";
import Orientation from "./pages/Orientation";
import AdminPage from "./pages/admin/AdminPage";
function App() {
  return (
    <Provider>
      <div className="flex min-h-screen flex-col">
        <Header />
        <MessageDialog />
        <div className="flex-1">
          <Routes>
            <Route
              path="/posts/:id/edit"
              element={
                <Protected>
                  <EditPost />
                </Protected>
              }
            />
            <Route
              path="/members"
              element={
                <Protected>
                  <Members />
                </Protected>
              }
            />
            <Route
              path="/members/:id"
              element={
                <Protected>
                  <MemberProfile />
                </Protected>
              }
            />
            <Route path="/posts/:id" element={<PostDetail />} />
            <Route
              path="/mypage/password"
              element={
                <Protected>
                  <ChangePassword />
                </Protected>
              }
            />
            <Route
              path="/critique/write"
              element={
                <Protected>
                  <ComposePost category="CRITIQUE" />
                </Protected>
              }
            />
            <Route
              path="/board/write"
              element={
                <Protected>
                  <ComposePost category="BOARD" />
                </Protected>
              }
            />
            <Route path="/" element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/activities" element={<Activities />} />
            <Route path="/share" element={<Share />} />
            <Route path="/login" element={<Login />} />
            <Route path="/officers" element={<Officers />} />
            <Route
              path="/share/upload"
              element={
                <Protected>
                  <UploadPhoto />
                </Protected>
              }
            />
            <Route
              path="/mypage"
              element={
                <Protected>
                  <MyPage />
                </Protected>
              }
            />
            <Route
              path="/mypage/photos"
              element={
                <Protected>
                  <MyPhotos />
                </Protected>
              }
            />
            <Route
              path="/equipment"
              element={
                <Protected>
                  <Equipment />
                </Protected>
              }
            />
            <Route
              path="/ot"
              element={
                <Protected>
                  <Orientation />
                </Protected>
              }
            />
            <Route
              path="/admin"
              element={
                <Protected admin>
                  <AdminPage />
                </Protected>
              }
            />
            {[
              ["critique", "CRITIQUE", "크리틱"],
              ["board", "BOARD", "게시판"],
            ].map(([path, category, title]) => (
              <Route
                key={path}
                path={"/" + path}
                element={
                  <Protected>
                    <Board key={category} category={category} title={title} />
                  </Protected>
                }
              />
            ))}
            <Route
              path="*"
              element={
                <main className="shell page-body">
                  <h1>페이지를 찾을 수 없습니다.</h1>
                </main>
              }
            />
          </Routes>
        </div>
        <footer className="border-t border-slate-200 p-8 text-center text-xs tracking-wide text-slate-500">
          © {new Date().getFullYear()} GUDO SIMDO · GACHON UNIVERSITY
          PHOTOGRAPHY CLUB
        </footer>
      </div>
    </Provider>
  );
}
createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
);
