import { useEffect, useState } from "react";
import ImageUpload from "./ImageUpload";

const ProductForm = ({ product, onSubmit }) => {

  const [formData, setFormData] = useState({
    productName: "",
    description: "",
    price: "",

    productType: "PT",
    billingType: "ONE_TIME",
    productStatus: "ACTIVE",
    category: "",

    duration: "",
    sessionCount: "",
  });

  useEffect(() => {
    if (product) {
      setFormData({
        productName: product.productName,
        description: product.description,
        price: product.price,
        productType: product.productType,
        billingType: product.billingType,
        productStatus: product.productStatus,
        category: product.category,

        duration: product.duration ?? "",
        sessionCount: product.sessionCount ?? "",
      });
    }
  }, [product]);

  const changeHandler = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => {
      const next = {
        ...prev,
        [name]: value,
      };

      if (name === "productType") {
        if (value === "PREMIUM") {
          next.billingType = "SUBSCRIPTION";
        } else {
          next.billingType = "ONE_TIME";
        }
        if (value !== "GYM") {
          next.duration = "";
        }
        if (value !== "PT") {
          next.sessionCount = "";
        }
        if (value !== "GOODS") {
          next.category = "";
        }
      }
      return next;
    });
  };

  const submitHandler = (e) => {
    e.preventDefault();

    onSubmit(formData);
  };

  return (
    <form onSubmit={submitHandler} className="product-form">

      <div>
        <label>상품명</label>
        <input
          type="text"
          name="productName"
          value={formData.productName}
          onChange={changeHandler}
        />
      </div>

      <div>
        <label>상품설명</label>
        <textarea
          name="description"
          value={formData.description}
          onChange={changeHandler}
        />
      </div>

      <div>
        <label>가격</label>
        <input
          type="number"
          name="price"
          value={formData.price}
          onChange={changeHandler}
        />
      </div>

      <div>
        <label>상품종류</label>
        <select
          name="productType"
          value={formData.productType}
          onChange={changeHandler}
        >
          <option value="PT">PT</option>
          <option value="GYM">GYM</option>
          <option value="GOODS">GOODS</option>
          <option value="PREMIUM">PREMIUM</option>
        </select>
      </div>

      {/* PT / GYM 기간 */}
      {(formData.productType === "GYM") && (
        <div>
          <label>이용기간(일)</label>
          <input
            type="number"
            name="duration"
            value={formData.duration}
            onChange={changeHandler}
          />
        </div>
      )}

      {/* PT 횟수 */}
      {formData.productType === "PT" && (
        <div>
          <label>PT 횟수</label>
          <input
            type="number"
            name="sessionCount"
            value={formData.sessionCount}
            onChange={changeHandler}
          />
        </div>
      )}
      {formData.productType === "GOODS" && (
        <div>
          <label>카테고리</label>
          <select
            name="category"
            value={formData.category}
            onChange={changeHandler}
          >
            <option value="">선택</option>
            <option value="운동기구">운동기구</option>
            <option value="식품">식품</option>
            <option value="트레이닝복">트레이닝복</option>
          </select>
        </div>
      )}
      <div>
        <label>상품상태</label>
        <select
          name="productStatus"
          value={formData.productStatus}
          onChange={changeHandler}
        >
          <option value="ACTIVE">판매중</option>
          <option value="SOLDOUT">품절</option>
        </select>
      </div>
      <button type="submit">
        {product ? "수정" : "등록"}
      </button>

    </form>
  );
};

export default ProductForm;