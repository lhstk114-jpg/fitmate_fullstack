import { useLocation } from "react-router-dom";
import OrderMembership from "../../../components/shop/order/OrderMembership";
import "../../../css/shop/order/orderMembership.css";

const OrderMembershipPage = () => {
  const location = useLocation();
  const product = location.state?.product;
  return <OrderMembership product={product} />;
};

export default OrderMembershipPage;
