import React, { useEffect, useState } from "react";
import { API_SERVER_URL } from "../../apis/commonApi";
import jwtAxios from "../../apis/util/jwtUtil";
import "../../css/admin/AdminChatBot.css";
import { useNavigate, useParams } from "react-router-dom";
import PageGenerate from "../common/Page/PageGenerate";
import ChatBotInsertModal from "./chatbot/ChatBotInsertModal";
import ChatBotUpdateModal from "./chatbot/ChatBotUpdateModal";

const AdminChatBotDetail = () => {
  const { id } = useParams();
  //chat값들을 저장할 상태값List
  const [chatList, setChatList] = useState(null);
  const navigate = useNavigate();
  //검색, 필터링 상태값
  const [subject, setSubject] = useState("");
  const [search, setSearch] = useState("");

  //modal창 제어용 bool값
  const [isBoolInsert, setIsBoolInsert] = useState(false);

  const [isBoolUpdate, setIsBoolUpdate] = useState(false);

  //updateModal용 id저장값
  const [answerId, setAnswerId] = useState(null);

  const handleSearchSubmit = (e) => {
    e.preventDefault(); // 폼 제출 시 페이지 새로고침 방지
    getAnswerList(search, subject, 0);
  };
  const insertUrl = `${API_SERVER_URL}/api/chatbot/insert/answer/${id}`;
  const updateUrl = `${API_SERVER_URL}/api/chatbot/update/answer/${id}`;

  const getAnswerList = async (search, subject, page) => {
    //있을때나 없을때나 실행할수있게 설정
    const url = `${API_SERVER_URL}/api/chatbot/list/answer/${id}?page=${page}&size=5&subject=${subject ? subject : ""}&search=${encodeURIComponent(search ? search : "")}`;
    // console.log(url);
    try {
      const res = await jwtAxios.get(url);
      console.log(res);
      setChatList(res.data.chatList);
      // console.log(res.data);
    } catch (err) {
      alert("에러발생 : " + err);
    }
  };

  const onDeleteAnswerFn = async (id) => {
    const agree = confirm("검색단어를 삭제하시겠습니까?");
    if (!agree) return;
    const url = `${API_SERVER_URL}/api/chatbot/delete/answer/${id}`;
    // console.log(url);
    try {
      const res = await jwtAxios.delete(url);
      if (res.data === "ok") {
        alert("삭제 성공");
      }
      getAnswerList("", "", 0);
    } catch (err) {
      alert("에러발생 : " + err);
    }
  };
  useEffect(() => {
    getAnswerList("", "", 0);
  }, []);
  return (
    <>
      {isBoolInsert === true && (
        <ChatBotInsertModal
          id={id}
          url={insertUrl}
          setIsBool={setIsBoolInsert}
          getList={getAnswerList}
        />
      )}
      {isBoolUpdate === true && (
        <ChatBotUpdateModal
          id={answerId}
          url={updateUrl}
          setIsBool={setIsBoolUpdate}
          getList={getAnswerList}
          isAnswer={true}
        />
      )}

      <div className="admin-chat">
        <div className="admin-chat-title-con">
          <h2 className="admin-chat-title">챗봇(답변) 관리</h2>
          <div className="chatInsert">
            <button onClick={() => setIsBoolInsert(true)}>
              답변예약어생성
            </button>
          </div>
        </div>
        {chatList !== null ? (
          <>
            <div className="header-con">
              <div className="search">
                <div className="filters">
                  <form onSubmit={handleSearchSubmit}>
                    <select
                      name="subject"
                      value={subject}
                      onChange={(e) => setSubject(e.target.value)}
                    >
                      <option value="">::선택::</option>
                      <option value="name">답변예약어</option>
                      <option value="content">내용</option>
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
            <div className="chatList">
              <ul className="chatList-answer-head">
                <li>답변예약어</li>
                <li>내용</li>
                <li>수정</li>
                <li>삭제</li>
              </ul>
              {chatList?.length > 0 ? (
                <>
                  {chatList.map((el, idx) => {
                    return (
                      <ul className="chatList-answer-body" key={idx}>
                        <li>{el.name}</li>
                        <li>{el.content}</li>
                        <li>
                          <button
                            onClick={() => {
                              setIsBoolUpdate(true);
                              setAnswerId(el.id);
                            }}
                          >
                            수정
                          </button>
                        </li>
                        <li>
                          <button onClick={() => onDeleteAnswerFn(el.id)}>
                            삭제
                          </button>
                        </li>
                      </ul>
                    );
                  })}
                  <ul className="chatList-foot">
                    <PageGenerate
                      currentPage={chatList.currentPage} //현재 페이지
                      startPage={chatList.startPage} //시작 페이지
                      endPage={chatList.endPage} //끝 페이지
                      totalPage={chatList.totalPage} //전체 페이지
                      onPageChange={getAnswerList} //리스트를 불러오는 함수
                      search={search} //검색어
                      subject={subject} //검색필터
                    />
                  </ul>
                </>
              ) : (
                <ul className="chatList-body-none">
                  <li>데이터가 존재하지 않습니다.</li>
                </ul>
              )}
            </div>
          </>
        ) : (
          <>Chat리스트를 불러오는 중입니다.</>
        )}
      </div>
    </>
  );
};

export default AdminChatBotDetail;
