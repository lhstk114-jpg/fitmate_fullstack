package org.spring.backend.shop.product.service;

import java.util.List;

import org.spring.backend.shop.product.dto.ProductDto;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.type.ImageType;
import org.spring.backend.shop.product.type.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

  void insertProduct(ProductDto productDto, MultipartFile thumbnail, List<MultipartFile> main, List<MultipartFile> details);

  void updateProduct(Long productId, ProductDto productDto, MultipartFile thumbnail, List<MultipartFile> main, List<MultipartFile> details);

  void deleteProduct(Long productId);

  // 상품 상세 조회
  ProductDto productDetail(Long productId);

  // 카테고리별 조회, 전체조회
  Page<ProductDto> productList(ProductType productType, Pageable pageable, String search);

  // 이미지 한장만 삭제
  void deleteImage(Long productFileId);

  // 이미지 전체삭제
  void deleteImages(Long productId);

  ProductDto getPremiumProduct();

  List<ProductDto> getTopSalesProducts();
}
