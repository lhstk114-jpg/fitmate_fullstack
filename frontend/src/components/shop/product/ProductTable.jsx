import ProductRow from "./ProductRow";


const ProductTable = ({ products, onEdit, onDelete }) => {
  return (
    <table className="product-table">
      <thead>
        <tr>
          <th>번호</th>
          <th>썸네일</th>
          <th>상품명</th>
          <th>카테고리</th>
          <th>상품종류</th>
          <th>가격</th>
          <th>상태</th>
          <th>관리</th>
        </tr>
      </thead>

      <tbody>
        {products.length === 0 ? (
          <tr>
            <td colSpan="9">등록된 상품이 없습니다.</td>
          </tr>
        ) : (
          products.map((product) => (
            <ProductRow
              key={product.id}
              product={product}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))
        )}
      </tbody>
    </table>
  );
};

export default ProductTable;