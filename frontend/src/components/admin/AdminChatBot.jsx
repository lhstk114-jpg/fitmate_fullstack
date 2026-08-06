import React, { useEffect, useState } from "react";
import { API_SERVER_URL } from "../../apis/commonApi";
import jwtAxios from "../../apis/util/jwtUtil";
import "../../css/admin/AdminChatBot.css";
import { useNavigate } from "react-router-dom";
import PageGenerate from "../common/Page/PageGenerate";
import ChatBotInsertModal from "./chatbot/ChatBotInsertModal";
import ChatBotUpdateModal from "./chatbot/ChatBotUpdateModal";

const AdminChatBot = () => {
  //chat값들을 저장할 상태값List
  const [chatData, setChatData] = useState(null);
  const navigate = useNavigate();
  //검색, 필터링 상태값
  const [subject, setSubject] = useState("");
  const [search, setSearch] = useState("");

  //modal창 제어용 bool값
  const [isBoolInsert, setIsBoolInsert] = useState(false);

  const [isBoolUpdate, setIsBoolUpdate] = useState(false);

  //updateModal용 id저장값
  const [id, setId] = useState(null);

  const handleSearchSubmit = (e) => {
    e.preventDefault(); // 폼 제출 시 페이지 새로고침 방지
    getChatData(search, subject, 0);
  };
  const insertUrl = `${API_SERVER_URL}/api/chatbot/insert/chat`;
  const updateUrl = `${API_SERVER_URL}/api/chatbot/update/chat`;

  const getChatData = async (search, subject, page) => {
    //있을때나 없을때나 실행할수있게 설정
    const url = `${API_SERVER_URL}/api/chatbot/list/chat?page=${page}&size=5&subject=${subject ? subject : ""}&search=${encodeURIComponent(search ? search : "")}`;
    // console.log(url);
    try {
      const res = await jwtAxios.get(url);
      setChatData(res.data);
      console.log(res.data);
    } catch (err) {
      alert("에러발생 : " + err);
    }
  };

  const onDeleteChatFn = async (id) => {
    const agree = confirm("검색단어를 삭제하시겠습니까?");
    if (!agree) return;
    const url = `${API_SERVER_URL}/api/chatbot/delete/chat/${id}`;
    // console.log(url);
    try {
      const res = await jwtAxios.delete(url);
      if (res.data === "ok") {
        alert("삭제 성공");
      }
      getChatData("", "", 0);
    } catch (err) {
      alert("에러발생 : " + err);
    }
  };
  useEffect(() => {
    getChatData("", "", 0);
  }, []);
  return (
    <>
      {isBoolInsert === true && (
        <ChatBotInsertModal
          url={insertUrl}
          setIsBool={setIsBoolInsert}
          getList={getChatData}
        />
      )}
      {isBoolUpdate === true && (
        <ChatBotUpdateModal
          id={id}
          url={updateUrl}
          setIsBool={setIsBoolUpdate}
          getList={getChatData}
          isAnswer={false}
        />
      )}
      <div className="admin-chat">
        <div className="admin-chat-title-con">
          <h2 className="admin-chat-title">챗봇(질문) 관리</h2>
          <div className="chatInsert">
            <button onClick={() => setIsBoolInsert(true)}>검색단어생성</button>
          </div>
        </div>
        {chatData !== null ? (
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
                      <option value="search">검색단어</option>
                      <option value="resStr">답변</option>
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
              <ul className="chatList-head">
                <li>검색단어</li>
                <li>답변</li>
                <li>수정</li>
                <li>답변예약어보기</li>
                <li>삭제</li>
              </ul>
              {chatData?.length !== null ? (
                <>
                  {chatData.chatList.map((el, idx) => {
                    return (
                      <ul className="chatList-body" key={idx}>
                        <li>{el.search}</li>
                        <li>{el.resStr}</li>
                        <li>
                          <button
                            onClick={() => {
                              setId(el.id);
                              setIsBoolUpdate(true);
                            }}
                          >
                            수정하기
                          </button>
                        </li>
                        <li>
                          <button
                            onClick={() =>
                              navigate(`/admin/chatbot/detail/${el.id}`)
                            }
                          >
                            답변보기
                          </button>
                        </li>
                        <li>
                          <button onClick={() => onDeleteChatFn(el.id)}>
                            삭제
                          </button>
                        </li>
                      </ul>
                    );
                  })}
                  <ul className="chatList-foot">
                    <PageGenerate
                      currentPage={chatData.currentPage} //현재 페이지
                      startPage={chatData.startPage} //시작 페이지
                      endPage={chatData.endPage} //끝 페이지
                      totalPage={chatData.totalPage} //전체 페이지
                      onPageChange={getChatData} //리스트를 불러오는 함수
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

export default AdminChatBot;
