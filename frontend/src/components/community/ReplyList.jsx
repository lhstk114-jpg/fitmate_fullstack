import axios from "axios";
import React, { useEffect, useState } from "react";
import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";
import { getCookie } from "../../apis/util/cookieUtil";

/**
 * 댓글 목록 컴포넌트
 * - 게시글(communityId)에 달린 댓글들을 조회해 표시
 * - 각 댓글마다 작성자 본인/관리자 여부에 따라 수정/삭제 버튼 노출
 * - 댓글별로 인라인 수정 모드를 지원 (editingId로 어떤 댓글이 수정 중인지 관리)
 * props:
 * - communityId: 댓글이 달린 게시글 id
 * - refreshKey: 값이 바뀔 때마다 목록을 강제로 재조회하기 위한 트리거 (Reply 컴포넌트에서 전달)
 */
const ReplyList = ({ communityId, refreshKey }) => {
  const [replies, setReplies] = useState([]); // 댓글 목록
  const [isLoading, setIsLoading] = useState(true);
  const [userName, setUserName] = useState(""); // 로그인 사용자 이름 (현재 화면에서 직접 사용되진 않음)
  const [userEmail, setUserEmail] = useState(""); // 로그인 사용자 이메일 (현재 화면에서 직접 사용되진 않음)
  const [editingId, setEditingId] = useState(null); // 현재 수정 중인 댓글 id (null이면 수정 중인 댓글 없음)
  const [editContent, setEditContent] = useState(""); // 수정 중인 댓글의 임시 입력값

  // 마운트 시 로그인 사용자 정보 조회 (참고용, 실제 권한 판단은 쿠키의 member로 별도 수행)
  useEffect(() => {
    getUser();
  }, []);

  const getUser = async () => {
    try {
      const res = await jwtAxios.get(`${API_SERVER_URL}/api/member/detail`);
      if (res.data?.result) {
        setUserName(res.data.result.userName);
        setUserEmail(res.data.result.userEmail);
      }
    } catch (error) {
      console.error("비로그인 상태입니다..");
    }
  };

  // 댓글 목록 조회
  const getReplyList = async () => {
    try {
      setIsLoading(true);
      // count=false : 댓글 조회는 게시글 조회수에 영향을 주지 않음
      const res = await axios.get(
        `${API_SERVER_URL}/api/reply/list/${communityId}?count=false`,
      );
      setReplies(res.data?.replies || res.data?.result || []);
    } catch (error) {
      alert(error);
      setReplies([]);
    } finally {
      setIsLoading(false);
    }
  };

  // communityId가 바뀌거나(다른 게시글로 이동) refreshKey가 바뀔 때(댓글 작성 후) 재조회
  useEffect(() => {
    if (communityId) {
      getReplyList();
    }
  }, [communityId, refreshKey]);

  // 현재 로그인한 사용자 정보 (댓글 목록 전체에 공통으로 쓰임, 본인/관리자 여부 판단용)
  const member = getCookie("member");
  const currentUserEmail = member?.userEmail
    ? decodeURIComponent(member.userEmail)
    : "";
  const isAdmin = member?.role === "ADMIN";

  // 수정 모드 진입: 해당 댓글의 id/내용을 편집 상태로 세팅
  const startEdit = (reply) => {
    setEditingId(reply.id);
    setEditContent(reply.content);
  };

  // 수정 취소: 편집 상태 초기화
  const cancelEdit = () => {
    setEditingId(null);
    setEditContent("");
  };

  // 수정 저장: 서버에 수정 요청 후 로컬 상태(replies)도 즉시 반영
  const saveEdit = async (reply) => {
    if (!editContent.trim()) {
      alert("댓글 내용을 입력하세요.");
      return;
    }
    try {
      await jwtAxios.put(`${API_SERVER_URL}/api/reply/update/${reply.id}`, {
        content: editContent,
        communityId: reply.communityId,
      });
      // 서버 재조회 없이 로컬 목록에서 해당 댓글 내용만 교체 (빠른 반영)
      setReplies((prev) =>
        prev.map((item) =>
          item.id === reply.id ? { ...item, content: editContent } : item,
        ),
      );
      cancelEdit();
    } catch (error) {
      console.error(error);
      alert("댓글 수정 실패");
    }
  };

  // 댓글 삭제: 확인 후 서버에 삭제 요청, 성공 시 로컬 목록에서도 제거
  const deleteEdit = async (reply) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      await jwtAxios.delete(`${API_SERVER_URL}/api/reply/delete/${reply.id}`, {
        communityId: reply.communityId,
      });
      setReplies((prev) => prev.filter((item) => item.id !== reply.id));
      alert("삭제되었습니다");
    } catch (error) {
      console.error(error);
      alert("댓글 삭제 실패");
    }
  };

  if (isLoading) {
    return <p>댓글을 불러오는 중입니다</p>;
  }

  if (replies.length === 0) {
    return <p className="reply-empty">아직 댓글이 없습니다</p>;
  }

  return (
    <div className="reply-list">
      {replies.map((reply) => {
        // ★ 각 댓글마다 개별적으로 본인/관리자 여부를 판단 (댓글 작성자가 서로 다르므로)
        const isOwner =
          currentUserEmail && currentUserEmail === reply.userEmail;
        const canManage = isOwner || isAdmin;

        return (
          <div key={reply.id}>
            {editingId === reply.id ? (
              // 현재 댓글이 수정 모드인 경우: 입력창 + 저장/취소 버튼 표시
              <div className="reply-edit">
                <input
                  type="text"
                  value={editContent}
                  onChange={(e) => setEditContent(e.target.value)}
                  autoFocus
                />
                <button type="button" onClick={() => saveEdit(reply)}>
                  저장
                </button>
                <button type="button" onClick={cancelEdit}>
                  취소
                </button>
              </div>
            ) : (
              // 일반 보기 모드: 댓글 내용 + 작성자 + (권한에 따라) 수정/삭제 버튼
              <>
                <div className="reply-content">{reply.content}</div>
                <div className="reply-meta">
                  <input name="userName" value={reply.userName} readOnly />
                  {isOwner && (
                    <button type="button" onClick={() => startEdit(reply)}>
                      수정
                    </button>
                  )}
                  {canManage && (
                    <button type="button" onClick={() => deleteEdit(reply)}>
                      삭제
                    </button>
                  )}
                </div>
              </>
            )}
          </div>
        );
      })}
    </div>
  );
};

export default ReplyList;
