import axios from "axios";
import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Reply from "./Reply";
import "../../css/Community/CommunityDetail.css";
import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";
import { getCookie } from "../../apis/util/cookieUtil.jsx";

/**
 * 일반 사용자용 게시글 상세 페이지
 * - URL 파라미터의 id로 게시글을 조회해 보여줌
 * - 작성자 본인 또는 관리자 여부에 따라 수정/삭제 버튼 노출
 * - 하단에 댓글(Reply) 컴포넌트 포함
 */
//12//
const CommunityDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [community, setCommunity] = useState(null); // 게시글 상세 데이터
  const [isLoading, setIsLoading] = useState(true);

  // 게시글 작성자 이메일과 비교, 권한 관리자 확인
  const member = getCookie("member"); // 로그인 사용자 쿠키 정보
  const currentUserEmail = member?.userEmail
    ? decodeURIComponent(member.userEmail)
    : "";
  const isAdmin = member?.role === "ADMIN";
  const isOwner = currentUserEmail && currentUserEmail === community?.userEmail;

  // ★ adminOnly(공지사항 등 관리자 전용 탭) 설정이면 관리자만 관리(삭제) 가능,
  //   일반 탭이면 작성자 본인 또는 관리자가 관리 가능
  const canManage = community?.tabAdminOnly ? isAdmin : isOwner || isAdmin;

  //상세정보 보기
  const getCommunityDetail = async () => {
    try {
      setIsLoading(true);
      // count=true : 상세 조회 시 조회수(hit)를 증가시키는 파라미터
      const res = await axios.get(
        `${API_SERVER_URL}/api/community/detail/${id}?count=true`,
      );
      if (res.data?.community) {
        setCommunity(res.data.community);
      }
    } catch (error) {
      // 존재하지 않는 게시글 등 조회 실패 시 목록으로 이동
      alert("존재하지 않는 게시글입니다");
      navigate("/community/communityList");
    } finally {
      setIsLoading(false);
    }
  };
  // id가 바뀔 때마다(다른 게시글로 이동 시) 상세 정보를 새로 조회
  useEffect(() => {
    getCommunityDetail();
  }, [id]);

  //게시글 삭제
  const getCommunityDelete = async () => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      setIsLoading(true);
      const res = await jwtAxios.delete(
        `${API_SERVER_URL}/api/community/delete/${id}`,
      );
      if (res.data?.result) {
        setCommunity(res.data.result);
        navigate("/community/index");
      }
    } catch (error) {
      alert("삭제 시도 중 오류가 발생했습니다");
      navigate("/community/index");
    } finally {
      setIsLoading(false);
    }
  };

  // content 안의 상대경로 이미지(src="/upload/...")를
  // 백엔드 서버 주소 기준 절대경로로 보정 (그래야 이미지가 정상적으로 표시됨)
  const renderableContent = (community?.content || "").replace(
    /src="\/upload\//g,
    `src="${API_SERVER_URL}/upload/`,
  );

  return (
    <>
      <div className="communityDetail">
        <div className="communityDetail-con">
          <h1>게시글 상세 페이지</h1>
          {isLoading ? (
            <p>데이터를 불러오는 중입니다</p>
          ) : community ? (
            <div className="detailbody">
              {/* 제목 (1줄 배치, 읽기 전용) */}
              <div className="form-row">
                <label>제목</label>
                <input type="text" value={community.title || ""} readOnly />
              </div>

              {/* 탭 + 카테고리 (같은 줄 배치, 읽기 전용) */}
              <div className="form-group-row">
                <div className="flex-item">
                  <label>탭 이름</label>
                  <input type="text" value={community.tabName || ""} readOnly />
                </div>
                <div className="flex-item">
                  <label>카테고리 이름</label>
                  <input
                    type="text"
                    value={community.categoryName || ""}
                    readOnly
                  />
                </div>
              </div>

              {/* 작성자 + 조회수 (같은 줄 배치, 읽기 전용) */}
              <div className="form-group-row">
                <div className="flex-item">
                  <label>작성자</label>
                  <input
                    type="text"
                    value={community.userName || ""}
                    readOnly
                  />
                </div>
                <div className="flex-item">
                  <label>조회수</label>
                  <input type="text" value={community.hit || ""} readOnly />
                </div>
              </div>

              {/* 내용 (보더라인 적용, HTML 렌더링) */}
              <div className="form-row">
                <label>내용</label>
                <div
                  className="content-view"
                  dangerouslySetInnerHTML={{ __html: renderableContent }}
                />
              </div>

              {/* 날짜 표시: 수정일이 있으면 수정일 우선, 없으면 작성일 */}
              <div className="form-row">
                <label>날짜</label>
                <div className="view-box">
                  {community.updateTime
                    ? `수정일: ${community.updateTime?.split("T")[0] || ""}`
                    : community.createTime
                      ? `작성일: ${community.createTime?.split("T")[0] || ""}`
                      : "날짜 정보 없음"}
                </div>
              </div>

              {/* 버튼 영역: 수정(작성자 본인만) / 목록으로 / 삭제(canManage 권한만) */}
              <div className="button-group">
                {isOwner && (
                  <button
                    onClick={() =>
                      navigate(`/community/update/${community.id}`)
                    }
                  >
                    수정
                  </button>
                )}
                <button onClick={() => navigate("/community/communityList")}>
                  목록으로 돌아가기
                </button>
                {canManage && (
                  <button onClick={() => getCommunityDelete()}>삭제</button>
                )}
              </div>
            </div>
          ) : (
            <p>게시글 정보가 없습니다</p>
          )}
        </div>
        {/* 댓글 영역: 게시글 id와 카테고리명을 전달 (카테고리에 따라 댓글 작성 제한 등에 사용) */}
        <Reply communityId={id} categoryName={community?.categoryName} />
      </div>
    </>
  );
};

export default CommunityDetail;
