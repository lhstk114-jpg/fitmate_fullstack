import { useEffect, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { getProductDetail } from "../../../apis/shop/productApi";
import { addCart } from "../../../apis/shop/cartApi";
import { getCookie } from "../../../apis/util/cookieUtil";
import ProductDetail from "../../../components/shop/product/ProductDetail";
import CartModal from "../../../components/shop/cart/CartModal";

const ProductDetailPage = () => {
  const { productId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [showCartModal, setShowCartModal] = useState(false);
  const [product, setProduct] = useState(null);

  // 수량 상태
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    loadProduct();
  }, [productId]);

  const loadProduct = async () => {
    try {
      const res = await getProductDetail(productId);
      setProduct(res.data);
    } catch (e) {
      console.error(e);
    }
  };

  // 장바구니 추가
  const handleCart = async () => {
    const member = getCookie("member");

    if (!member) {
      alert("로그인이 필요합니다.");

      navigate("/auth/login", {
        state: {
          from: location.pathname + location.search,
        },
      });

      return;
    }

    try {
      await addCart({
        productId: product.id,
        quantity: quantity,
      });
      setShowCartModal(true);
    } catch (e) {
      console.error(e);
    }
  };

  // 바로 구매
  const handleBuy = async () => {
    const member = getCookie("member");
    if (!member) {
      alert("로그인이 필요합니다.");
      navigate("/auth/login", {
        state: {
          from: location.pathname + location.search,
        },
      });
      return;
    }

    const directItem = {
      id: product.id,
      productId: product.id,
      productName: product.productName,
      productType: product.productType,
      price: product.price,
      productImage: product.fileDtos.find(
        (file) => file.imageType === "THUMBNAIL",
      )?.newFileName,
      quantity: product.productType === "GOODS" ? quantity : 1,
    };

    if (product.productType === "GOODS") {
      navigate("/order", {
        state: {
          directItem,
        },
      });
    } else {
      // PT / GYM / PREMIUM 공통
      navigate("/order/membership", {
        state: {
          product,
        },
      });
    }
  };

  if (!product) return <div>Loading...</div>;

  return (
    <div className="product-detail">
      <div className="product-detail-con">
        <ProductDetail
          product={product}
          quantity={quantity}
          setQuantity={setQuantity}
          handleCart={handleCart}
          handleBuy={handleBuy}
        />
        {showCartModal && (
          <CartModal
            onContinue={() => {
              setShowCartModal(false);
            }}
            onCart={() => {
              navigate("/cart");
            }}
          />
        )}
      </div>
    </div>
  );
};

export default ProductDetailPage;
