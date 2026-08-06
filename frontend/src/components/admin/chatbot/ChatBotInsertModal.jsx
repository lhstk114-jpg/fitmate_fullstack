import React, { useState } from "react";
import "../../../css/chatbot/chatBotModal.css";
import axios from "axios";

const initChatData = {
  resStr: "",
  search: "",
  keywordType: "CATEGORY",
};
const initAnswerData = {
  name: "",
  content: "",
};

const ChatBotInsertModal = ({ url, setIsBool, id, getList }) => {
  const isAnswer = Boolean(id);
  // console.log(isAnswer);
  // console.log(id);
  //대주제 저장용 data
  const [chatData, setChatData] = useState(initChatData);
  //답변 저장용 data
  const [answerData, setAnswerData] = useState(initAnswerData);

  //기본 데이터 onChange함수
  const onChangeFn = (e) => {
    const { name, value } = e.target;
    if (isAnswer) {
      setAnswerData({ ...answerData, [name]: value });
    } else {
      setChatData({ ...chatData, [name]: value });
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
  return (
    <div className="chatModal">
      <div className="chatModal-con">
        <ul>
          {id ? (
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
          </li>
        </ul>
      </div>
    </div>
  );
};

export default ChatBotInsertModal;
