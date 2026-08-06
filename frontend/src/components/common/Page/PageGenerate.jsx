import "../../../css/common/PageGenerate.css";

const PageGenerate = (props) => {
  const currentPage = props.currentPage; //현재페이지
  const endPage = props.endPage; //끝페이지
  const totalPage = props.totalPage; //전체페이지수
  const startPage = props.startPage; //시작페이지
  const onPageChange = props.onPageChange; //검색함수(url주소를 반환해주기위함)
  const search = props.search; //검색어
  const subject = props.subject; //필터값

  //페이지 번호 컴포넌트를 담을 빈 배열
  const pageNumbers = [];

  //버튼들을 배열에 push작업
  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(
      <button
        key={i}
        className={`page-btn ${currentPage === i - 1 ? "active" : ""}`}
        onClick={() => onPageChange(search, subject, i - 1)}
      >
        {i}
      </button>,
    );
  }
  return (
    <div className="pagination">
      {/* 이전페이지 버튼(startPage가 1보다 클때만 보임) */}
      {startPage > 1 && (
        <button onClick={() => onPageChange(search, subject, currentPage - 1)}>
          이전
        </button>
      )}
      {/* 페이지번호 배열 출력 */}
      {pageNumbers}
      {/* 다음페이지 버튼(endPage가 totalPage보다 작을때만 보임) */}
      {endPage < totalPage && (
        <button onClick={() => onPageChange(search, subject, currentPage + 1)}>
          다음
        </button>
      )}
    </div>
  );
};

export default PageGenerate;
