import { Outlet, useNavigate } from "react-router-dom";
import AdminHeader from "../components/admin/AdminHeader";
import AdminLeft from "../components/admin/AdminLeft";
import "../css/admin/Admin.css";
import { useSelector } from "react-redux";
import { useEffect, useState } from "react";
import "../css/admin/AdminLayout.css";

const AdminLayout = () => {
  const navigate = useNavigate();
  //authSlice에 저장된 멤버데이터를 가져옴
  const { memberData } = useSelector((state) => state.loginSlice);

  const role = memberData?.result?.role;
  const isLogin = !!memberData?.result?.userEmail;

  const isAdminOrManager = role === "ADMIN" || role === "MANAGER";
  const hasAccess = isLogin && isAdminOrManager;

  useEffect(() => {
    if (!hasAccess) {
      alert("접근 권한이 없습니다.");
      navigate("/", { replace: true });
    }
  }, [hasAccess, navigate]);

  //모바일인지 확인할 상태값
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  //사이드바 제어용 함수
  const toggleSidebar = () => setIsSidebarOpen((prev) => !prev);
  const closeSidebar = () => setIsSidebarOpen(false);

  if (!hasAccess) return null;

  return (
    <>
      <AdminHeader onToggleSidebar={toggleSidebar} />

      <Outlet />

      {/* 모바일에서 어두운 배경 클릭 시 닫기 */}
      <div
        className={`sidebar-overlay ${isSidebarOpen ? "open" : ""}`}
        onClick={closeSidebar}
      />

      {/* 사이드바에 isOpen 상태와 closeSidebar 함수 전달 */}
      <AdminLeft isOpen={isSidebarOpen} onClose={closeSidebar} />
    </>
  );
};

export default AdminLayout;
