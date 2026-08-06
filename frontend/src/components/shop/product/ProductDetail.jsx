import React from 'react'
import { API_SERVER_URL } from '../../../apis/commonApi';

const ProductDetail = ({
  product,
  quantity,
  setQuantity,
  handleCart,
  handleBuy
}) => {
  const main = product.fileDtos.find(
    file => file.imageType === "MAIN"
  ) || product.fileDtos[0];

  const details = product.fileDtos.filter(
    file => file.imageType === "DETAIL"
  );

  return (
    <>
      {/* 메인 이미지 */}
      {main && (
        <img
          src={`${API_SERVER_URL}/upload/product/${main.newFileName}`}
          alt={product.productName}
          className="main-image"
        />
      )}

      <h2 className="productName"> {product.productName}</h2>

      <p className="price">{product.price.toLocaleString()}원</p>

      <p className="description">{product.description}</p>

      {/* 수량 조절*/}
      {product.productType === "GOODS" && (
        <div className="quantity-box">
          <button
            onClick={() =>
              setQuantity(prev => Math.max(1, prev - 1))
            }> - </button>

          <span>
            {quantity}
          </span>

          <button
            onClick={() =>
              setQuantity(prev => prev + 1)} >  + </button>
        </div>
      )}
      {/* 버튼 */}
      <div className="purchase-box">

        {product.productType === "GOODS" && (
          <button
            className="cart-button"
            onClick={handleCart}
          >
            장바구니
          </button>
        )}

        <button
          className="buy-button"
          onClick={handleBuy}
        >
          {product.productType === "PREMIUM"
            ? "구독 시작"
            : product.productType === "PT" || product.productType === "GYM"
              ? "이용권 구매"
              : "바로 구매"}
        </button>

      </div>

      {/* 상세 이미지 */}
      {details.map(detail => (
        <img
          key={detail.id}
          src={`${API_SERVER_URL}/upload/product/${detail.newFileName}`}
          alt="상세"
          className="detail-image"
        />
      ))}

    </>
  )
}

export default ProductDetail