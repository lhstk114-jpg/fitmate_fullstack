import axios from "axios";
import React, { useEffect, useState } from "react";
import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";

/**
 * 댓글 작성 폼 컴포넌트
 * - 로그인 사용자 정보를 자동으로 불러와 작성자로 세팅
 * - QNA 카테고리 게시글은 관리자만 댓글 작성 가능하도록 제한
 * props:
 * - communityId: 댓글을 달 게시글 id
 * - categoryName: 게시글 카테고리명 (QNA 여부 판단)
 * - onReplyAdd: 댓글 작성 성공 시 호출되는 콜백 (부모의 댓글 목록 갱신 트리거)
 */
const ReplyForm = ({ communityId, categoryName, onReplyAdd }) => {
  const [reply, setReply] = useState({
    memberId: "",
    content: "",
    communityId,
    userName: "",
    userEmail: "",
  });
  const [role, setRole] = useState(null); // 사용자 role ("ADMIN" 또는 "USER" 등)
  const [loadingUser, setLoadingUser] = useState(true); // 사용자 정보 로딩 여부

  // 마운트 시 로그인 사용자 정보 조회
  useEffect(() => {
    getUser();
  }, []);

  const getUser = async () => {
    try {
      const res = await jwtAxios.get(`${API_SERVER_URL}/api/member/detail`);
      if (res && res.data && res.data.result) {
        setReply((prev) => ({
          ...prev,
          memberId: res.data.result.memberId,
          userName: res.data.result.userName,
          userEmail: res.data.result.userEmail,
        }));
        setRole(res.data.result.role); // "ADMIN" 또는 "USER" 등
      }
    } catch (error) {
      // 비로그인 상태 등으로 사용자 정보 조회 실패 시 비회원으로 표시
      console.error("회원 정보를 불러올 수 없습니다.", error);
      setReply((prev) => ({ ...prev, userName: "비회원" }));
    } finally {
      setLoadingUser(false);
    }
  };

  // 카테고리명에 "qna"가 포함되어 있으면 QNA 게시글로 판단
  const isQna = categoryName && categoryName.toLowerCase().includes("qna");
  const isAdmin = role === "ADMIN";
  // QNA 게시글이면서 관리자가 아니면 댓글 작성 차단
  const isBlocked = isQna && !isAdmin;

  // 댓글 저장 요청
  const saveReply = async () => {
    if (isBlocked) {
      alert("QNA 게시글의 댓글은 관리자만 작성할 수 있습니다.");
      return;
    }
    if (!reply.content.trim()) {
      alert("댓글 내용을 입력하세요");
      return;
    }
    try {
      const res = await jwtAxios.post(
        `${API_SERVER_URL}/api/reply/insert`,
        reply,
      );
      alert("댓글이 작성되었습니다");
      setReply({ ...reply, content: "" }); // 입력창 초기화
      if (onReplyAdd) {
        onReplyAdd(res.data); // 부모(Reply)에 작성 완료를 알려 목록 갱신
      }
    } catch (error) {
      if (error?.response?.status === 403) {
        // 서버에서도 QNA 권한을 검증하므로 프론트 우회 시도를 재차 차단
        alert("QNA 게시글의 댓글은 관리자만 작성할 수 있습니다.");
      } else {
        alert("댓글 작성 실패");
      }
    }
  };

  // 사용자 정보 로딩 중에는 아무것도 렌더링하지 않음 (깜빡임 방지)
  if (loadingUser) return null;

  // QNA + 비관리자인 경우 입력 폼 대신 안내 문구만 표시
  if (isBlocked) {
    return (
      <div className="reply-write reply-write--blocked">
        <p>QNA 게시글의 댓글은 관리자만 작성할 수 있습니다.</p>
      </div>
    );
  }

  return (
    <div className="reply-write">
      <ul>
        <li>
          <input
            type="text"
            value={reply.content}
            onChange={(e) => setReply({ ...reply, content: e.target.value })}
            placeholder="댓글을 입력하세요"
          />
          {/* 작성자명 표시 (읽기 전용, 로그인 정보 기반 자동 세팅) */}
          <input name="userName" value={reply.userName} readOnly />
          <button type="button" onClick={saveReply}>
            댓글 작성
          </button>
        </li>
      </ul>
    </div>
  );
};

export default ReplyForm;
