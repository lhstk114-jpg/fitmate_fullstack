import { useEffect, useState } from "react";
import OrderList from "../../../components/shop/order/OrderList";
import jwtAxios from "../../../apis/util/jwtUtil";
import "../../../css/shop/order/OrderList.css";

const OrderListPage = () => {
  const [orders, setOrders] = useState([]);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const res = await jwtAxios.get("/api/order/list");
        setOrders(res.data);
      } catch (err) {
        console.error(err);
      }
    };

    fetchOrders();
  }, []);

  return <OrderList orders={orders} />;
};

export default OrderListPage;
