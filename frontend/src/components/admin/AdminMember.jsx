import { useNavigate } from "react-router-dom";
import "../../css/admin/AdminMember.css";
import { useEffect, useState } from "react";
import PageGenerate from "../common/Page/PageGenerate";
import { API_SERVER_URL } from "../../apis/commonApi";
import jwtAxios from "../../apis/util/jwtUtil";
import AdminMemberInsertModal from "./member/AdminMemberInsertModal";

const AdminMember = () => {
  const navigate = useNavigate();
  //멤버데이터 상태값
  const [memberData, setMemberData] = useState(null);
  //필터 상태값
  const [subject, setSubject] = useState("");
  //검색어 상태값
  const [search, setSearch] = useState("");
  //기본검색어 중 권한값을 MEMBER로 초기화
  const [role, setRole] = useState("MEMBER");
  //모달창 플래그값
  const [isBool, setIsBool] = useState(false);
  const handleSearchSubmit = (e) => {
    e.preventDefault(); // 폼 제출 시 페이지 새로고침 방지
    getMemberList(search, subject, 0);
  };
  const getMemberList = async (search, subject, page) => {
    //있을때나 없을때나 실행할수있게 설정
    const url = `${API_SERVER_URL}/api/member/admin/memberList?page=${page}&size=5&subject=${subject ? subject : ""}&role=${role}&search=${encodeURIComponent(search ? search : "")}`;
    // console.log(url);
    try {
      const res = await jwtAxios.get(url);
      setMemberData(res.data);
      // console.log(res.data);
    } catch (err) {
      alert("에러발생 : " + err);
    }
  };
  //권한버튼 누를때마다 새로 리스트를 받아오게 설정
  useEffect(() => {
    getMemberList("", "", 0);
  }, [role]);
  return memberData !== null ? (
    <div className="admin-member">
      <div className="admin-member-con">
        <div className="admin-member-title-con">
          <h2 className="admin-member-title">회원 관리</h2>
          <div className="insertMember">
            <button onClick={() => setIsBool(true)}>회원추가</button>
          </div>
        </div>
        {isBool && (
          <AdminMemberInsertModal
            getMemberList={getMemberList}
            setIsBool={setIsBool}
          />
        )}
        <div className="header-con">
          <div className="roleFilter">
            <button
              className={role === "MEMBER" ? "active" : ""}
              onClick={() => setRole("MEMBER")}
            >
              일반회원
            </button>
            <button
              className={role === "TRAINER" ? "active" : ""}
              onClick={() => setRole("TRAINER")}
            >
              트레이너
            </button>
            <button
              className={role === "MANAGER" ? "active" : ""}
              onClick={() => setRole("MANAGER")}
            >
              매니저
            </button>
            <button
              className={role === "ADMIN" ? "active" : ""}
              onClick={() => setRole("ADMIN")}
            >
              관리자
            </button>
          </div>
          <div className="search">
            <div className="filters">
              <form onSubmit={handleSearchSubmit}>
                <select
                  name="subject"
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                >
                  <option value="">::선택::</option>
                  <option value="userName">이름</option>
                  <option value="userEmail">이메일</option>
                  <option value="role">권한</option>
                </select>

                <input
                  type="text"
                  name="search"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="검색어를 입력하세요"
                />

                <input type="submit" value="검색" />
              </form>
            </div>
          </div>
        </div>
        <div className="memberList">
          <ul className="memberList-head">
            <li>이름</li>
            <li>이메일</li>
            <li>권한</li>
            <li>상세보기</li>
          </ul>
          {memberData?.memberList?.map((el, idx) => {
            return (
              <ul className="memberList-body" key={idx}>
                <li>{el.userName}</li>
                <li>{el.userEmail}</li>
                <li>{el.role}</li>
                <li>
                  <button
                    onClick={() => navigate(`/admin/member/detail/${el.id}`)}
                  >
                    상세보기
                  </button>
                </li>
              </ul>
            );
          })}
          <ul className="memberList-foot">
            <PageGenerate
              currentPage={memberData.currentPage} //현재 페이지
              startPage={memberData.startPage} //시작 페이지
              endPage={memberData.endPage} //끝 페이지
              totalPage={memberData.totalPage} //전체 페이지
              onPageChange={getMemberList} //리스트를 불러오는 함수
              search={search} //검색어
              subject={subject} //검색필터
            />
          </ul>
        </div>
      </div>
    </div>
  ) : (
    <>회원정보를 불러오는 중입니다.</>
  );
};

export default AdminMember;
