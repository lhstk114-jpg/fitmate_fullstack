import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { getProductList } from "../../../apis/shop/productApi";
import ProductCard from "../../../components/shop/product/ProductCard";

import "../../../css/shop/product/ProductListPage.css";

const ProductListPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const productType = searchParams.get("productType");
  const category = searchParams.get("category");

  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);

  useEffect(() => {
    loadProducts();
  }, [productType, page]);

  const loadProducts = async () => {
    try {
      const res = await getProductList(productType, page, 100);
      setProducts(res.data.content);
    } catch (err) {
      console.error(err);
    }
  };
  const filteredProducts = category
    ? products.filter((product) => product.category === category)
    : products;
  return (
    <>
      <div className="product-list">
        <div className="product-list-con">
          {filteredProducts.length === 0 ? (
            <div className="no-product">해당 카테고리에 상품이 없습니다.</div>
          ) : (
            filteredProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))
          )}
        </div>
      </div>
    </>
  );
};

export default ProductListPage;
