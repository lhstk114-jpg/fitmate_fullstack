import { Link } from "react-router-dom";
import { API_SERVER_URL } from "../../../apis/commonApi";

const OrderList = ({ orders }) => {

  const formatDateTime = (dateTime) => {
    if (!dateTime) return "날짜 미상";
    const date = new Date(dateTime);
    return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, "0")}.${String(date.getDate()).padStart(2, "0")}`;
  };

  if (!orders || orders.length === 0) {
    return <p className="no-orders">주문 내역이 없습니다.</p>;
  }

  // 같은 주문 일시 기준 그룹화
  const groupedOrders = orders.reduce((groups, order) => {
    const date = order.createTime || "날짜 미상";
    if (!groups[date]) {
      groups[date] = [];
    }
    groups[date].push(order);
    return groups;
  }, {});

  // 최신 주문 먼저 표시
  const sortedDates = Object.keys(groupedOrders).sort((a, b) => b.localeCompare(a));

  return (
    <div className="order-list">
      <h2>주문 내역</h2>
      {sortedDates.map((date) => (
        // 날짜별 그룹을 감싸는 컨테이너
        <div key={date} className="order-date-group">

          {/* 같은 날짜에 주문한 것들의 공통 헤더 */}
          <div className="order-group-header">
            <span className="order-date">
              {formatDateTime(date)}
            </span>
            <Link to={`/order/detail/${groupedOrders[date][0].id}`} className="order-detail">
              주문상세
            </Link>
          </div>

          {/* 해당 날짜에 속한 주문 카드들을 반복 출력 */}
          <div className="order-cards-container">
            {groupedOrders[date].map((order) => (
              <div className="order-card" key={order.id}>

                {/* 하나의 주문 카드 안에 들어있는 여러 상품들 출력 */}
                {order.orderItemDtos?.map((item) => {
                  const imageSrc = item.productImage
                    ? `${API_SERVER_URL}/upload/product/${item.productImage}`
                    : null;

                  return (
                    <Link
                      to={`/products/detail/${item.productId}`}
                      className="order-content"
                      key={item.id || item.productName}>
                      {/* 이미지가 있을 때만 렌더링 */}
                      {item?.productImage && (
                        <img
                          src={imageSrc}
                          alt={item?.productName || "상품 정보 없음"}
                          className="order-thumbnail"
                        />
                      )}

                      <div className="order-item-info">
                        <h4>{item?.productName || "등록된 상품 정보가 없습니다"}</h4>
                        <p className="price">
                          {item.price?.toLocaleString()}원
                        </p>
                      </div>
                    </Link>
                  );
                })}

                {/* 주문 카드 하단 배송 정보 */}
                <div className="order-status">
                  <span>배송: {order.deliveryStatus}</span>
                </div>
              </div>
            ))}
          </div>

        </div>
      ))}
    </div>
  );
};

export default OrderList;