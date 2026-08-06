import { useEffect, useRef, useState } from "react";
import "../../css/chatbot/chatbot.css";
import SockJS from "sockjs-client";
import { Stomp } from "@stomp/stompjs";
import { API_SERVER_URL } from "../../apis/commonApi";

const ChatBot = () => {
  //채팅on, off조절용 상태값
  const [isOpen, setIsOpen] = useState(false);
  //메시지 목록을 관리할 상태값
  const [messages, setMessages] = useState([]);
  //질문을 관리할 상태값
  const [question, setQuestion] = useState("");

  //DOM 요소에 접근하기 위한 Ref
  const chatContentRef = useRef(null);
  //채팅창에 focus를 두기위한 Ref
  const questionRef = useRef(null);
  //stompClient가 리렌더링시에도 유지될수있게 설정
  const stompClient = useRef(null);

  //메세지 전송 함수
  const msgSendClickFn = (e) => {
    //일반 확인 이벤트 무효화처리
    e.preventDefault();
    //질문내용 없으면 함수종료
    if (!question.trim()) return;

    //메시지들을 객체로 합침
    const userMessage = {
      sender: "user",
      text: question,
      time: new Date().toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      }),
    };

    //사용자의 질문메시지 표시
    showMessageFn(userMessage);

    // 3. stompClientRef.current 사용 및 inputVal -> question 수정
    if (stompClient.current && stompClient.current.connected) {
      stompClient.current.send(
        "/app/message",
        {},
        JSON.stringify({ content: question }),
      );
    } else {
      alert("소켓 연결이 완료되지 않았습니다.");
    }

    //입력창 초기화
    setQuestion("");

    //채팅창 포커스
    questionRef.current?.focus();
  };
  //메세지 전송 함수(RabbitMQ)
  const rabbitMsgSendClickFn = (e) => {
    //일반 확인 이벤트 무효화처리
    e.preventDefault();
    //질문내용 없으면 함수종료
    if (!question.trim()) return;

    //메시지들을 객체로 합침
    const userMessage = {
      sender: "user",
      text: question,
      time: new Date().toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      }),
    };

    //사용자의 질문메시지 표시
    showMessageFn(userMessage);

    // 3. stompClientRef.current 사용 및 inputVal -> question 수정
    if (stompClient.current && stompClient.current.connected) {
      stompClient.current.send(
        "/app/bot",
        {},
        JSON.stringify({ content: question }),
      );
    } else {
      alert("소켓 연결이 완료되지 않았습니다.");
    }

    //입력창 초기화
    setQuestion("");

    //채팅창 포커스
    questionRef.current?.focus();
  };

  //소켓통신 연결 함수
  const onConnectFn = async () => {
    setIsOpen(true);
    const serverUrl = import.meta.env.BACKEND_API_SERVER_URL || "";
    const socketUrl = `${serverUrl}/api/chatEndpoint`;
    //웹소켓 연결, transports 옵션을 주어 폴백 순서를 제어
    const socket = () =>
      new SockJS(socketUrl, null, {
        transports: ["websocket", "xhr-streaming", "xhr-polling"],
      });
    //STOMP 클라이언트 생성
    stompClient.current = Stomp.over(socket);
    stompClient.current.connect(
      {},
      (frame) => {
        console.log("connected:", frame);
        //최초 연결 시 서버에서 인사메세지 호출
        stompClient.current.send(
          `/app/hello`,
          {},
          JSON.stringify({ content: "hello" }),
        );
        //백엔드에서 지정한 응답들 구독
        stompClient.current.subscribe(`/topic/message`, (message) => {
          const body = JSON.parse(message.body);
          //챗봇 응답이 text(응답)과 time(시간)으로 나눠져있음
          showMessageFn({ sender: "bot", text: body.content, time: body.time });
        });
        stompClient.current.subscribe(`/topic/greetings`, (message) => {
          const body = JSON.parse(message.body);
          showMessageFn({ sender: "bot", text: body.content, time: body.time });
        });
        stompClient.current.subscribe(`/topic/question`, (message) => {
          const body = JSON.parse(message.body);
          // 1. 기본 시스템 메시지 (responseText)
          let fullText = body.responseText || body.content || "";

          // 2. answerList(세부 답변 목록)가 존재하면 텍스트 뒤에 덧붙이기
          if (body.answerList && body.answerList.length > 0) {
            fullText += "\n\n" + body.answerList.join("\n");
          }

          // 3. 화면 출력 함수 호출 (text에는 합친 문자열, time에는 서버 전달 시간 지정)
          showMessageFn({
            sender: "bot",
            text: body.responseText,
            answers: body.answerList || [], // answerList 배열을 그대로 전달
            time: body.formattedTime || body.time,
          });
        });
        stompClient.current.subscribe(`/topic/notification`, (message) => {
          const body = JSON.parse(message.body);
          showMessageFn({ sender: "bot", text: body.content, time: body.time });
        });
      },
      (err) => {
        console.error("stomp연결 실패", err);
        alert("서버 연결 실패");
      },
    );
  };
  //채팅 메시지 화면 출력 함수
  const showMessageFn = (message) => {
    setMessages((prevMessages) => [...prevMessages, message]);
  };

  //웹소켓 종료함수
  const disconnectFn = () => {
    setIsOpen(false);
    if (stompClient.current) {
      stompClient.current.disconnect(() => {
        console.log("Disconnected");
      });
      stompClient.current = null;
    }
    setMessages([]);
  };

  //메시지 목록이 업데이트될 때마다 자동으로 스크롤을 맨 아래로 이동
  useEffect(() => {
    if (chatContentRef.current) {
      chatContentRef.current.scrollTop = chatContentRef.current.scrollHeight;
    }
  }, [messages]);
  return (
    <div className="chat">
      <div id="chat-bot">
        <div className="wrap">
          {!isOpen ? (
            <button type="button" id="btn-chat-open" onClick={onConnectFn}>
              OPEN
            </button>
          ) : (
            <div id="chat-disp">
              <div id="chat-disp-con">
                <div id="chat-header">
                  <span>Chat-Bot(WebSocket)</span>
                  <button id="close" type="button" onClick={disconnectFn}>
                    X
                  </button>
                </div>
                <div id="chat-content" ref={chatContentRef}>
                  {messages.map((msg, idx) => (
                    <div
                      key={msg.id || idx}
                      className={`msg-wrapper ${msg.sender}`}
                    >
                      {/* 서버 메시지인 경우 */}
                      {msg.sender === "bot" ? (
                        <div className="bot-msg-box">
                          <div className="head-img">
                            <img
                              src="/images/chatbot/chatbot.png"
                              alt="챗봇 프로필"
                            />
                          </div>
                          <div className="message">
                            {/* 1. 기본 시스템 메시지 */}
                            <div>{msg.text}</div>

                            {/* 2. 세부 답변 목록이 존재할 경우 하나씩 줄바꿈하여 출력 */}
                            {msg.answers && msg.answers.length > 0 && (
                              <div
                                className="answer-list"
                                style={{ marginTop: "8px" }}
                              >
                                {msg.answers.map((answer, index) => (
                                  <div key={index} className="answer-item">
                                    {answer}
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                          <div className="time">{msg.time}</div>
                        </div>
                      ) : (
                        /* 유저 메시지인 경우 */
                        <div className="user-msg-box">
                          <span className="time">{msg.time}</span>
                          <span className="content">{msg.text}</span>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
                <div id="chat-question" className="flex between">
                  <input
                    type="text"
                    id="question"
                    placeholder="질문을 입력하세요"
                    ref={questionRef}
                    value={question}
                    onChange={(e) => setQuestion(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        e.preventDefault(); // 페이지 새로고침 방지
                        if (!e.nativeEvent.isComposing) {
                          // 한글 중복/오작동 방지
                          rabbitMsgSendClickFn(e);
                        }
                      }
                    }}
                  />
                  {/* webSocket전송버튼 */}
                  {/* <button
                      id="btn-msg-send"
                      type="button"
                      onClick={msgSendClickFn}
                    >
                      전송
                    </button> */}
                  <button
                    id="rabbit-btn-msg-send"
                    type="button"
                    onClick={rabbitMsgSendClickFn}
                  >
                    전송
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ChatBot;
