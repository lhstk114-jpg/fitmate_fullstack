import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAdminProductList, deleteProduct } from "../../apis/shop/productApi";
import ProductTable from "../shop/product/ProductTable";
import PageGenerate from "../common/Page/PageGenerate";

const AdminProductList = () => {
  const navigate = useNavigate();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [pageInfo, setPageInfo] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);

  useEffect(() => {
    loadProducts(currentPage);
  }, [currentPage]);

  const loadProducts = async (page) => {
    try {
      setLoading(true);
      const res = await getAdminProductList(page, 10);
      setProducts(res.data.content);
      setPageInfo(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };
  const handlePageChange = (search, subject, page) => {
    setCurrentPage(page);
  };
  const handleEdit = (product) => {
    navigate(`/admin/product/update/${product.id}`);
  };

  const handleDelete = async (productId) => {
    if (!window.confirm("상품을 삭제하시겠습니까?")) return;

    try {
      await deleteProduct(productId);
      setProducts((prev) => prev.filter((product) => product.id !== productId));
      alert("삭제되었습니다.");
    } catch (e) {
      console.error(e);
      alert("삭제 실패");
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <>
      <div className="admin-product-header">
        <h2>상품 관리</h2>
        <button
          className="admin-product-insert-btn"
          onClick={() => navigate("/admin/product/insert")}
        >
          상품 등록
        </button>
      </div>
      <div className="product-table-wrapper">
        <ProductTable
          products={products}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      </div>
      {pageInfo && (
        <PageGenerate
          currentPage={pageInfo.number}
          startPage={Math.floor(pageInfo.number / 10) * 10 + 1}
          endPage={Math.min(
            Math.floor(pageInfo.number / 10) * 10 + 10,
            pageInfo.totalPages,
          )}
          totalPage={pageInfo.totalPages}
          onPageChange={handlePageChange}
          search=""
          subject=""
        />
      )}
    </>
  );
};

export default AdminProductList;
