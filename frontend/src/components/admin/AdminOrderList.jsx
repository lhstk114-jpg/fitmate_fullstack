import { useEffect, useState } from "react";
import axios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";
import PageGenerate from "../common/Page/PageGenerate";

const AdminOrderList = () => {
  const [orders, setOrders] = useState([]);
  const [pageInfo, setPageInfo] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);

  useEffect(() => {
    getOrders();
  }, [currentPage]);
  const isSubscriptionProduct = (order) => {
    const type = order.orderItemDtos?.[0]?.productType;

    return type === "GYM" || type === "PT" || type === "PREMIUM";
  };
  const getOrders = async () => {
    try {
      const res = await axios.get(`${API_SERVER_URL}/api/order/orderList`, {
        params: {
          page: currentPage,
          size: 10,
        },
      });

      setOrders(res.data.content);
      setPageInfo(res.data);
    } catch (error) {
      console.log("주문 조회 실패", error);
    }
  };

  const changeStatus = async (id, status) => {
    try {
      await axios.patch(
        `${API_SERVER_URL}/api/order/${id}/delivery-status`,
        null,
        {
          params: {
            deliveryStatus: status,
          },
        },
      );
      getOrders();
    } catch (error) {
      console.log(error);
    }
  };

  return (
    <div className="admin-order-page">
      <div className="admin-order-con">
        <h2 className="admin-order-title">주문 관리</h2>

        <div className="admin-order-table-wrapper">
          <table className="admin-order-table">
            <thead>
              <tr>
                <th>주문번호</th>
                <th>회원</th>
                <th>상품명</th>
                <th>금액</th>
                <th>주문일</th>
                <th>배송관리</th>
              </tr>
            </thead>

            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.id}</td>

                  <td>
                    {order.memberName}
                    <br />
                    <small>{order.memberEmail}</small>
                  </td>

                  <td>
                    {order.orderItemDtos?.length > 0 &&
                      order.orderItemDtos[0].productName}

                    {order.orderItemDtos?.length > 1 &&
                      ` 외 ${order.orderItemDtos.length - 1}개`}
                  </td>

                  <td>{order.totalPrice?.toLocaleString()}원</td>

                  <td>{order.createTime?.substring(0, 10)}</td>

                  <td>
                    {isSubscriptionProduct(order) ? (
                      <span>구독 상품</span>
                    ) : (
                      <select
                        value={order.deliveryStatus}
                        onChange={(e) => changeStatus(order.id, e.target.value)}
                      >
                        <option value="READY">준비중</option>
                        <option value="SHIPPING">배송중</option>
                        <option value="COMPLETE">배송완료</option>
                      </select>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {pageInfo && (
            <PageGenerate
              currentPage={pageInfo.number}
              startPage={Math.floor(pageInfo.number / 10) * 10 + 1}
              endPage={Math.min(
                Math.floor(pageInfo.number / 10) * 10 + 10,
                pageInfo.totalPages,
              )}
              totalPage={pageInfo.totalPages}
              onPageChange={(search, subject, page) => setCurrentPage(page)}
              search=""
              subject=""
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminOrderList;
