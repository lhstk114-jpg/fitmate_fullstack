import React, { useEffect, useState } from "react";

import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";
import PageGenerate from "../common/Page/PageGenerate";
import "../../css/admin/AdminPopup.css";
//초기값 선언
const initState = {
  id: null,
  title: "",
  content: "",
  linkUrl: "",
  active: true,
  sortOrder: 0,
  startDate: "",
  endDate: "",
  attachFile: null,
  newFileName: "",
};

const AdminPopup = () => {
  // 팝업 목록
  // const [popupList, setPopupList] = useState([]);

  // 모달 입력값
  const [popup, setPopup] = useState({ ...initState });

  // 모달 열림 여부
  const [modalOpen, setModalOpen] = useState(false);

  // insert / update / delete
  const [modalType, setModalType] = useState("insert");

  // 선택한 이미지 미리보기
  const [previewUrl, setPreviewUrl] = useState("");

  // 종료시간 지난 팝업 강조
  const isExpired = (endDate) => {
    return new Date(endDate) < new Date();
  };

  // 팝업 목록 조회
  // const getPopupList = async () => {
  //   try {
  //     const res = await jwtAxios.get(`${API_SERVER_URL}/api/admin/popupList`);

  //     setPopupList(res.data.result || []);
  //   } catch (err) {
  //     console.error("팝업 목록 조회 실패", err);
  //   }
  // };

  //팝업 공용 페이징 추가
  const [popupData, setPopupData] = useState(null);
  const [subject, setSubject] = useState("");
  const [search, setSearch] = useState("");
  const handleSearchSubmit = (e) => {
    e.preventDefault(); // 폼 제출 시 페이지 새로고침 방지
    getPopupList(subject, search, 0);
    // 선택된 조건이 없거나 검색어가 비어있으면 전체 목록으로 이동하거나 알림 처리
    // if (!subject && search) {
    //   alert("검색 필터를 선택해주세요.");
    //   return;
    // }
  };

  const getPopupList = async (subject, search, page) => {
    //있을때나 없을때나 실행할수있게 설정
    const url = `${API_SERVER_URL}/api/admin/popupList?page=${page}&size=5&subject=${subject ? subject : ""}&search=${encodeURIComponent(search ? search : "")}`;
    try {
      const res = await jwtAxios.get(url);
      setPopupData(res.data);
      // setPopupList(popupData.popupList);
      console.log(res.data);
    } catch (err) {
      alert("에러발생 : " + err);
    }
  };
  //페이지 최초 실행 시 팝업 목록 조회
  useEffect(() => {
    getPopupList();
  }, []);

  // input 값 변경
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    setPopup((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  //이미지 파일 변경
  const handleFileChange = (e) => {
    const file = e.target.files[0];

    if (!file) return;

    setPopup((prev) => ({
      ...prev,
      attachFile: file,
    }));

    setPreviewUrl(URL.createObjectURL(file));
  };

  //등록 모달 열기
  const openInsertModal = () => {
    setPopup({ ...initState });
    setPreviewUrl("");
    setModalType("insert");
    setModalOpen(true);
  };

  //수정 모달 열기
  const openUpdateModal = (item) => {
    setPopup({
      id: item.id,
      title: item.title || "",
      content: item.content || "",
      linkUrl: item.linkUrl || "",
      active: item.active ?? true,
      sortOrder: item.sortOrder ?? 0,
      startDate: formatDateTimeLocal(item.startDate),
      endDate: formatDateTimeLocal(item.endDate),
      attachFile: null,
      newFileName: item.newFileName || "",
    });

    setPreviewUrl("");
    setModalType("update");
    setModalOpen(true);
  };

  //삭제 모달 열기
  const openDeleteModal = (item) => {
    setPopup({
      ...initState,
      id: item.id,
      title: item.title,
    });

    setModalType("delete");
    setModalOpen(true);
  };

  //모달 닫기
  const closeModal = () => {
    setModalOpen(false);
    setPopup({ ...initState });
    setPreviewUrl("");
  };

  //FormData 생성
  //파일과 일반 데이터를 같이 전달하기 위해 사용
  const createFormData = () => {
    const formData = new FormData();

    formData.append("title", popup.title);
    formData.append("content", popup.content);
    formData.append("linkUrl", popup.linkUrl);
    formData.append("active", popup.active);
    formData.append("sortOrder", Number(popup.sortOrder));

    if (popup.startDate) {
      formData.append("startDate", popup.startDate);
    }

    if (popup.endDate) {
      formData.append("endDate", popup.endDate);
    }

    if (popup.attachFile) {
      formData.append("attachFile", popup.attachFile);
    }
    if (popup.newFileName) {
      formData.append("newFileName", popup.newFileName);
    }

    return formData;
  };

  // 팝업 등록
  const insertPopup = async () => {
    try {
      const formData = createFormData();

      await jwtAxios.post(`${API_SERVER_URL}/api/admin/popupInsert`, formData);

      alert("팝업이 등록되었습니다.");

      closeModal();
      getPopupList();
    } catch (err) {
      console.error("팝업 등록 실패", err);
      console.error("응답:", err.response?.data);

      alert("팝업 등록에 실패했습니다.");
    }
  };

  //팝업 수정
  const updatePopup = async () => {
    try {
      const formData = createFormData();

      await jwtAxios.put(
        `${API_SERVER_URL}/api/admin/popupUpdate/${popup.id}`,
        formData,
      );

      alert("팝업이 수정되었습니다.");

      closeModal();
      getPopupList();
    } catch (err) {
      console.error("팝업 수정 실패", err);
      console.error("응답:", err.response?.data);

      alert("팝업 수정에 실패했습니다.");
    }
  };

  //팝업 삭제
  const deletePopup = async () => {
    try {
      await jwtAxios.delete(
        `${API_SERVER_URL}/api/admin/popupDelete/${popup.id}`,
      );

      alert("팝업이 삭제되었습니다.");

      closeModal();
      getPopupList();
    } catch (err) {
      console.error("팝업 삭제 실패", err);
      console.error("응답:", err.response?.data);

      alert("팝업 삭제에 실패했습니다.");
    }
  };

  //모달 종류에 따라 등록 / 수정 / 삭제 실행
  const handleSubmit = (e) => {
    e.preventDefault();

    if (modalType === "insert") {
      insertPopup();
      return;
    }

    if (modalType === "update") {
      updatePopup();
      return;
    }

    if (modalType === "delete") {
      deletePopup();
    }
  };

  // LocalDateTime 값을 datetime-local 형식으로 변환
  // 2026-07-14T10:30:00 -> 2026-07-14T10:30
  const formatDateTimeLocal = (dateTime) => {
    if (!dateTime) return "";

    return dateTime.slice(0, 16);
  };

  return (
    <div className="admin-popup">
      <div className="admin-popup-header">
        <div>
          <h2>팝업</h2>
        </div>

        <button
          type="button"
          className="popup-insert-btn"
          onClick={openInsertModal}
        >
          팝업 등록
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
              <option value="active">노출 여부</option>
              <option value="sortOrder">노출 순서</option>
              <option value="title">제목</option>
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
      <div className="admin-popup-table-wrap">
        <table className="admin-popup-table">
          <thead>
            <tr>
              <th>번호</th>
              <th>제목</th>
              <th>노출 여부</th>
              <th>노출 순서</th>
              <th>이미지</th>
              <th>노출 시작일</th>
              <th>노출 종료일</th>
              <th>관리</th>
            </tr>
          </thead>

          <tbody>
            {popupData?.popupList.length === 0 ? (
              <tr>
                <td colSpan={8} className="popup-empty">
                  등록된 팝업이 없습니다.
                </td>
              </tr>
            ) : (
              popupData?.popupList.map((popup) => {
                // 노출 종료일이 현재 시간보다 이전이면 true
                const isExpired =
                  popup.endDate && new Date(popup.endDate) < new Date();

                return (
                  <tr key={popup.id}>
                    <td>{popup.id}</td>

                    <td className="popup-title">{popup.title}</td>

                    <td>
                      <span
                        className={
                          !popup.active
                            ? "popup-status inactive"
                            : isExpired
                              ? "popup-status expired"
                              : "popup-status active"
                        }
                      >
                        {popup.active ? "노출" : "미노출"}
                      </span>
                    </td>

                    <td>{popup.sortOrder}</td>

                    <td>{popup.newFileName ? "O" : "X"}</td>

                    <td>
                      {popup.startDate
                        ? popup.startDate.replace("T", " ").slice(0, 16)
                        : "-"}
                    </td>

                    <td className={isExpired ? "popup-end-date expired" : ""}>
                      {popup.endDate
                        ? popup.endDate.replace("T", " ").slice(0, 16)
                        : "-"}
                    </td>

                    <td>
                      <div className="popup-manage-btns">
                        <button
                          type="button"
                          className="popup-update-btn"
                          onClick={() => openUpdateModal(popup)}
                        >
                          수정
                        </button>

                        <button
                          type="button"
                          className="popup-delete-btn"
                          onClick={() => openDeleteModal(popup)}
                        >
                          삭제
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
      <div className="admin-popup-bottom">
        <div className="admin-popup-paging">
          <PageGenerate
            currentPage={popupData?.currentPage} //현재 페이지
            startPage={popupData?.startPage} //시작 페이지
            endPage={popupData?.endPage} //끝 페이지
            totalPage={popupData?.totalPage} //전체 페이지
            onPageChange={getPopupList} //리스트를 불러오는 함수
            search={search} //검색어
            subject={subject} //검색필터
          />
        </div>
      </div>
      {modalOpen && (
        <div className="popup-modal-overlay" onClick={closeModal}>
          <div className="popup-modal" onClick={(e) => e.stopPropagation()}>
            <div className="popup-modal-header">
              <h3>
                {modalType === "insert" && "팝업 등록"}
                {modalType === "update" && "팝업 수정"}
                {modalType === "delete" && "팝업 삭제"}
              </h3>

              <button
                type="button"
                className="popup-modal-close"
                onClick={closeModal}
              >
                ×
              </button>
            </div>

            {modalType === "delete" ? (
              <form onSubmit={handleSubmit}>
                <div className="popup-delete-message">
                  <strong>{popup.title}</strong>
                  <p>해당 팝업을 삭제하시겠습니까?</p>
                </div>

                <div className="popup-modal-buttons">
                  <button type="button" onClick={closeModal}>
                    취소
                  </button>

                  <button type="submit" className="delete-confirm-btn">
                    삭제
                  </button>
                </div>
              </form>
            ) : (
              <form onSubmit={handleSubmit}>
                <div className="popup-modal-body">
                  {/* 왼쪽: 실시간 미리보기 */}
                  <div className="popup-preview-section">
                    <h4>팝업 미리보기</h4>

                    <div className="popup-preview-card">
                      <div className="popup-preview-image">
                        {previewUrl ? (
                          <img src={previewUrl} alt="팝업 미리보기" />
                        ) : popup.newFileName ? (
                          <img
                            src={`${API_SERVER_URL}/upload/popup/${popup.newFileName}`}
                            alt="기존 팝업 이미지"
                          />
                        ) : (
                          <div className="popup-preview-empty">
                            <span>이미지를 선택해주세요.</span>
                          </div>
                        )}
                      </div>

                      <div className="popup-preview-content">
                        <h3>{popup.title || "팝업 제목"}</h3>

                        <p>
                          {popup.content || "팝업 내용이 여기에 표시됩니다."}
                        </p>

                        <div className="popup-preview-link">
                          {popup.linkUrl || "이동 주소가 표시됩니다."}
                        </div>

                        <div className="popup-preview-buttons">
                          <button type="button">오늘 그만보기</button>
                          <button type="button" className="preview-close-btn">
                            닫기
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* 오른쪽: 입력폼 */}
                  <div className="popup-form-section">
                    <div className="popup-form">
                      <div className="popup-form-row">
                        <label htmlFor="title">팝업 제목</label>
                        <input
                          id="title"
                          name="title"
                          type="text"
                          value={popup.title}
                          onChange={handleChange}
                          required
                        />
                      </div>

                      <div className="popup-form-row">
                        <label htmlFor="content">팝업 내용</label>
                        <textarea
                          id="content"
                          name="content"
                          value={popup.content}
                          onChange={handleChange}
                        />
                      </div>

                      <div className="popup-form-row">
                        <label htmlFor="linkUrl">이동 주소</label>
                        <input
                          id="linkUrl"
                          name="linkUrl"
                          type="text"
                          value={popup.linkUrl}
                          onChange={handleChange}
                          placeholder="/community/detail/1"
                        />
                      </div>

                      <div className="popup-form-flex">
                        <div className="popup-form-row">
                          <label htmlFor="sortOrder">노출 순서</label>
                          <input
                            id="sortOrder"
                            name="sortOrder"
                            type="number"
                            min="0"
                            value={popup.sortOrder}
                            onChange={handleChange}
                          />
                        </div>

                        <div className="popup-form-row popup-active-row">
                          <label htmlFor="active">노출 여부</label>

                          <div className="popup-checkbox-wrap">
                            <input
                              id="active"
                              name="active"
                              type="checkbox"
                              checked={popup.active}
                              onChange={handleChange}
                            />
                            <span>{popup.active ? "노출" : "미노출"}</span>
                          </div>
                        </div>
                      </div>

                      <div className="popup-form-flex">
                        <div className="popup-form-row">
                          <label htmlFor="startDate">노출 시작일</label>
                          <input
                            id="startDate"
                            name="startDate"
                            type="datetime-local"
                            value={popup.startDate}
                            onChange={handleChange}
                          />
                        </div>

                        <div className="popup-form-row">
                          <label htmlFor="endDate">노출 종료일</label>
                          <input
                            id="endDate"
                            name="endDate"
                            type="datetime-local"
                            value={popup.endDate}
                            onChange={handleChange}
                          />
                        </div>
                      </div>

                      <div className="popup-form-row">
                        <label htmlFor="attachFile">팝업 이미지</label>
                        <input
                          id="attachFile"
                          name="attachFile"
                          type="file"
                          accept="image/*"
                          onChange={handleFileChange}
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <div className="popup-modal-buttons">
                  <button type="button" onClick={closeModal}>
                    취소
                  </button>

                  <button type="submit" className="popup-submit-btn">
                    {modalType === "insert" ? "등록" : "수정"}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminPopup;
