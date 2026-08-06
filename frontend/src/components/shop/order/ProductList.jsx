import React from 'react';
import { API_SERVER_URL } from '../../../apis/commonApi';

const ProductList = ({ cartItems }) => {

  return (
    <div className="productList">
      <h2>주문 상품</h2>

      {cartItems.map((item) => {

        const imageUrl = item.productImage
          ? item.productImage.startsWith("/upload")
            ? item.productImage
            : `/upload/product/${item.productImage}`
          : null;
        return (
          <div className="productItem" key={item.id}>

            {imageUrl && (
            <img
              src={`${API_SERVER_URL}${imageUrl}`}
              alt={item.productName}
            />)}

            <div className="productInfo">
              <h3>{item.productName}</h3>
              <p>수량 : {item.quantity}개</p>
            </div>

            <div className="productPrice">
              {(item.price * item.quantity).toLocaleString()}원
            </div>

          </div>
        );
      })}
    </div>
  );
};

export default ProductList;