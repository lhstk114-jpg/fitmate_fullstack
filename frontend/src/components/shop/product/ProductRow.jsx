import { Link, useNavigate } from "react-router-dom";
import { API_SERVER_URL } from "../../../apis/commonApi";

const ProductRow = ({ product, onEdit, onDelete }) => {
  const navigate = useNavigate();

  const thumbnail = product.fileDtos?.find(
    (file) => file.imageType === "THUMBNAIL",
  );

  return (
    <tr className="productRow">
      <td className="productId">{product.id}</td>
      <td className="thumbnail">
        <Link to={`/products/detail/${product.id}`}>
          {thumbnail ? (
            <img
              src={`${API_SERVER_URL}/upload/product/${thumbnail.newFileName}`}
              alt={product.productName}
              width={80}
              height={80}
              style={{
                objectFit: "cover",
                borderRadius: "6px",
              }}
            />
          ) : (
            <span>이미지 없음</span>
          )}
        </Link>
      </td>
      <td>{product.productName}</td>
      <td>{product.category}</td>
      <td>{product.productType}</td>
      <td>{product.price.toLocaleString()}원</td>
      <td>{product.productStatus}</td>

      <td>
        <button
          className="update"
          onClick={() => navigate(`/admin/product/update/${product.id}`)}
        >
          수정
        </button>

        <button className="delete" onClick={() => onDelete(product.id)}>
          삭제
        </button>
      </td>
    </tr>
  );
};

export default ProductRow;
