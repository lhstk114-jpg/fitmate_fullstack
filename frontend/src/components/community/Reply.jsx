import React, { useState } from "react";
import ReplyList from "./ReplyList";
import ReplyForm from "./ReplyForm";

/**
 * 댓글 영역 컨테이너 컴포넌트
 * - 댓글 목록(ReplyList)과 댓글 작성 폼(ReplyForm)을 함께 렌더링
 * - refreshKey를 이용해 새 댓글 작성 시 목록을 강제로 다시 조회하도록 트리거
 * props:
 * - communityId: 댓글이 달린 게시글 id
 * - categoryName: 게시글의 카테고리명 (QNA 등 댓글 작성 제한 판단에 사용)
 */
const Reply = ({ communityId, categoryName }) => {
  // 값이 바뀔 때마다 ReplyList의 useEffect가 재실행되도록 만드는 트리거 값
  const [refreshKey, setRefreshKey] = useState(0);

  // 댓글 작성 성공 시 ReplyForm에서 호출 → refreshKey를 증가시켜 목록 갱신
  const handleReplyAdd = () => {
    setRefreshKey((prev) => prev + 1);
  };
  return (
    <div className="reply">
      <div className="reply-con">
        <h1>댓글</h1>
        <ReplyList communityId={communityId} refreshKey={refreshKey} />
        <ReplyForm
          communityId={communityId}
          categoryName={categoryName}
          onReplyAdd={handleReplyAdd}
        />
      </div>
    </div>
  );
};

export default Reply;
