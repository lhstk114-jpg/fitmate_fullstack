const OrderDetail = ({ order }) => {
  if (!order) {
    return <p>주문 정보를 불러오는 중...</p>;
  }
  return (
    <div className="order-detail-page">

      <h2>주문 상세</h2>

      <div className="order-info">
        <p>주문번호 : {order.id}</p>
        <p>결제일시 : {
          order.createTime
            ? new Date(order.createTime).toLocaleString("ko-KR")
            : ""
        }</p>
      </div>

      <h3>주문 상품</h3>

      <ul className="order-product-list">
        {order.orderItemDtos?.map((item) => (
          <li key={item.id}>
            <span>{item.productName}</span>
            <span>{item.quantity}개</span>
            <span>{item.price.toLocaleString()}원</span>
          </li>
        ))}
      </ul>
      <div className="buyer-info">
        <h3>주문자 정보</h3>
        <p>주문자 : {order.memberName}</p>
        <p>연락처 : {order.memberPhone}</p>
        <p>이메일 : {order.memberEmail}</p>
      </div>

      <div className="payment-info">
        <h3>결제 정보</h3>
        <p>결제수단 : {order.paymentDto?.paymentMethod}</p>
        <p>총 결제금액 : {order.totalPrice?.toLocaleString()}원</p>
      </div>

      <div className="delivery-info">
        <h3>배송 정보</h3>

        <p>받는 사람 : {order.receiverName}</p>
        <p>주소: {order.receiverAddress} {order.receiverDetailAddress}</p>
        <p>연락처 : {order.receiverPhone}</p>
        <p>배송메모 : {order.deliveryMemo}</p>
      </div>

    </div>
  );
};

export default OrderDetail;