import React, { useEffect, useState } from "react";
import "../../../css/chatbot/chatBotModal.css";
import axios from "axios";
import { API_SERVER_URL } from "../../../apis/commonApi";

const initChatData = {
  resStr: "",
  search: "",
  keywordType: "CATEGORY",
};
const initAnswerData = {
  name: "",
  content: "",
};

const ChatBotUpdateModal = ({ id, url, setIsBool, isAnswer, getList }) => {
  // console.log(id);
  //대주제 저장용 data
  const [chatData, setChatData] = useState(initChatData);
  //답변 저장용 data
  const [answerData, setAnswerData] = useState(initAnswerData);

  const getUrl = isAnswer
    ? `${API_SERVER_URL}/api/chatbot/detail/answer/${id}`
    : `${API_SERVER_URL}/api/chatbot/detail/chat/${id}`;

  const deleteUrl = isAnswer
    ? `${API_SERVER_URL}/api/chatbot/delete/answer/${id}`
    : `${API_SERVER_URL}/api/chatbot/delete/chat/${id}`;

  //기본 데이터 onChange함수
  const onChangeFn = (e) => {
    const { name, value } = e.target;
    if (isAnswer) {
      setAnswerData({ ...answerData, [name]: value });
    } else {
      setChatData({ ...chatData, [name]: value });
    }
  };

  const getDataFn = async () => {
    try {
      const res = await axios.get(getUrl);
      console.log(res.data.result);
      if (isAnswer) {
        setAnswerData(res.data.result);
      } else {
        setChatData(res.data.result);
      }
    } catch (err) {
      console.error("통신 에러:", err);
      alert("서버 연결에 실패하였습니다.");
    }
  };

  const insertChatBotFn = async (data) => {
    // const url = `${API_SERVER_URL}/api/chatbot/insert/chat`;
    const agree = confirm("데이터를 저장하시겠습니까?");
    if (!agree) return;
    try {
      console.log(data);
      const formData = {
        ...data,
      };
      const res = await axios.post(url, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (res.data === "ok") {
        alert("저장에 성공하였습니다.");
        getList("", "", 0);
        setIsBool(false);
      }
    } catch (err) {
      console.error("통신 에러:", err);
      alert("서버 연결에 실패하였습니다.");
    }
  };

  //삭제
  const onDeleteFn = async () => {
    const agree = confirm("데이터를 삭제하시겠습니까?");
    if (!agree) return;
    try {
      const res = await axios.delete(deleteUrl);
      if (res.data === "ok") {
        alert("삭제에 성공하였습니다.");
        getList("", "", 0);
        setIsBool(false);
      }
    } catch (err) {
      console.error("통신 에러:", err);
      alert("서버 연결에 실패하였습니다.");
    }
  };
  useEffect(() => {
    getDataFn();
  }, [id, isAnswer]);
  return (
    <div className="chatModal">
      <div className="chatModal-con">
        <ul>
          {isAnswer ? (
            <>
              <li>
                <span>대주제</span>
                {/* <span>{data?.search}</span> */}
              </li>
              <li>
                <span>답변예약어</span>
                <span>
                  <input
                    type="text"
                    onChange={onChangeFn}
                    id="name"
                    name="name"
                    value={answerData.name}
                  />
                </span>
              </li>
              <li>
                <span>내용</span>
                <span>
                  <input
                    type="text"
                    onChange={onChangeFn}
                    id="content"
                    name="content"
                    value={answerData.content}
                  />
                </span>
              </li>
            </>
          ) : (
            <>
              <li>
                <span>검색단어</span>
                <span>
                  <input
                    type="text"
                    onChange={onChangeFn}
                    id="search"
                    name="search"
                    value={chatData.search}
                  />
                </span>
              </li>
              <li>
                <span>답변</span>
                <span>
                  <input
                    type="text"
                    onChange={onChangeFn}
                    id="resStr"
                    name="resStr"
                    value={chatData.resStr}
                  />
                </span>
              </li>
              <li>
                <span>카테고리</span>
                <span>
                  <select
                    name="keywordType"
                    id="keywordType"
                    value={chatData.keywordType}
                    onChange={onChangeFn}
                  >
                    <option value="CATEGORY">대주제</option>
                    <option value="ACTION">세부행동</option>
                  </select>
                </span>
              </li>
            </>
          )}
          <li>
            <button
              onClick={() => {
                isAnswer
                  ? insertChatBotFn(answerData)
                  : insertChatBotFn(chatData);
              }}
            >
              저장
            </button>
            <button
              onClick={() => {
                isAnswer
                  ? setAnswerData(initAnswerData)
                  : setChatData(initChatData);
              }}
            >
              초기화
            </button>
            <button onClick={() => setIsBool(false)}>취소</button>
            <button onClick={onDeleteFn}>삭제</button>
          </li>
        </ul>
      </div>
    </div>
  );
};

export default ChatBotUpdateModal;
