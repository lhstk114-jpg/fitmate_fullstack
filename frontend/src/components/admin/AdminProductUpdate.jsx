import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import {
  getProductDetail,
  updateProduct,
  deleteImage,
  deleteAllImages,
} from "../../apis/shop/productApi";

import ProductForm from "../../components/shop/product/ProductForm";
import ImageUpload from "../../components/shop/product/ImageUpload";
import "../../css/admin/AdminProduct.css";
import { API_SERVER_URL } from "../../apis/commonApi";

const AdminProductUpdatePage = () => {
  const { productId } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);

  const [images, setImages] = useState({
    thumbnail: null,
    main: [],
    details: [],
  });

  useEffect(() => {
    loadProduct();
  }, []);

  const loadProduct = async () => {
    try {
      const res = await getProductDetail(productId);

      setProduct(res.data);
    } catch (e) {
      console.error(e);
    }
  };

  // 이미지 추가
  const handleImageChange = (imageData) => {
    setImages(imageData);
  };

  // 이미지 개별 삭제
  const handleDeleteImage = async (fileId) => {
    try {
      await deleteImage(fileId);
      alert("이미지가 삭제되었습니다.");
      loadProduct();
    } catch (e) {
      console.error(e);
    }
  };

  // 이미지 전체 삭제
  const handleDeleteAllImages = async () => {
    if (!product.fileDtos || product.fileDtos.length === 0) {
      alert("등록된 이미지가 없습니다.");
      return;
    }
    if (!window.confirm("모든 이미지를 삭제하시겠습니까?")) {
      return;
    }
    try {
      await deleteAllImages(productId);
      setProduct((prev) => ({
        ...prev,
        fileDtos: [],
      }));
      alert("전체 삭제되었습니다.");
    } catch (e) {
      console.error(e);
    }
  };

  // 수정
  const handleSubmit = async (formData) => {
    const data = new FormData();

    data.append(
      "productDto",
      new Blob([JSON.stringify(formData)], {
        type: "application/json",
      }),
    );

    if (images.thumbnail) {
      data.append("thumbnail", images.thumbnail);
    }

    images.main.forEach((file) => {
      data.append("main", file);
    });

    images.details.forEach((file) => {
      data.append("details", file);
    });

    try {
      await updateProduct(productId, data);
      alert("수정되었습니다.");
      navigate("/admin/product");
    } catch (e) {
      console.error(e);
      alert("수정 실패");
    }
  };

  if (!product) {
    return <div>Loading...</div>;
  }

  return (
    <div className="admin-product-update">
      <h2>상품 수정</h2>
      <div className="product-content-wrapper">
        {/* 좌측 영역: 상품 정보 입력 폼 */}
        <ProductForm product={product} onSubmit={handleSubmit} />

        {/* 우측 영역: 이미지 관련 요소를 하나로 감싸기 */}
        <div className="image-manage-section">
          {/* 새 이미지 업로드 */}
          <ImageUpload onChange={handleImageChange} />

          {/* 기존 등록된 이미지 목록 카드 */}
          <div className="image-list">
            <div className="image-list-header">
              <h3>등록된 이미지 목록</h3>
              {product.fileDtos && product.fileDtos.length > 0 && (
                <button
                  type="button"
                  className="btn-delete-all"
                  onClick={handleDeleteAllImages}
                >
                  이미지 전체 삭제
                </button>
              )}
            </div>

            {product.fileDtos && product.fileDtos.length > 0 ? (
              <div className="image-grid">
                {product.fileDtos.map((file) => (
                  <div key={file.id} className="image-item">
                    <img
                      src={`${API_SERVER_URL}/upload/product/${file.newFileName}`}
                      alt="상품 이미지"
                    />
                    <button
                      type="button"
                      className="btn-delete-single"
                      onClick={() => handleDeleteImage(file.id)}
                    >
                      삭제
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <p className="no-images">등록된 이미지가 없습니다.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminProductUpdatePage;
