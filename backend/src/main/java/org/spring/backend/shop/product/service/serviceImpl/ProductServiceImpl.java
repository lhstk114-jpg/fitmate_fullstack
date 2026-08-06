package org.spring.backend.shop.product.service.serviceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.spring.backend.file.enumtype.TableType;
import org.spring.backend.file.handler.FileHandler;
import org.spring.backend.file.repository.FileRepository;
import org.spring.backend.shop.product.dto.ProductDto;
import org.spring.backend.shop.product.entity.ProductEntity;
import org.spring.backend.shop.product.repository.ProductRepository;
import org.spring.backend.shop.product.service.ProductService;
import org.spring.backend.shop.product.type.ImageType;
import org.spring.backend.shop.product.type.ProductType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

  @Value("${img.path.product}")
  private String itemPath;

  private final ProductRepository productRepository;
  private final FileRepository fileRepository;

  private final FileHandler fileHandler;

  @Override
  public void insertProduct(ProductDto productDto, MultipartFile thumbnail, List<MultipartFile> main,
      List<MultipartFile> details) {

    if (productRepository.existsByProductName(productDto.getProductName())) {
      throw new IllegalArgumentException("이미 등록된 상품입니다.");
    }
    // PREMIUM 상품 중복 등록 방지
    if (productDto.getProductType() == ProductType.PREMIUM
        && productRepository.existsByProductType(ProductType.PREMIUM)) {

      throw new IllegalArgumentException(
          "프리미엄 상품은 하나만 등록 가능합니다.");
    }

    ProductEntity productEntity = ProductEntity.builder()
        .productName(productDto.getProductName())
        .price(productDto.getPrice())
        .description(productDto.getDescription())
        .productType(productDto.getProductType())
        .billingType(productDto.getBillingType())
        .productStatus(productDto.getProductStatus())
        .category(productDto.getCategory())
        .duration(productDto.getDuration())
        .sessionCount(productDto.getSessionCount())
        .build();

    productRepository.save(productEntity);

    try {
      fileHandler.insertFile(
          itemPath,
          TableType.PRODUCT,
          productEntity.getId(),
          thumbnail,
          ImageType.THUMBNAIL,
          1);

      if (main != null) {
        for (int i = 0; i < main.size(); i++) {
          fileHandler.insertFile(
              itemPath,
              TableType.PRODUCT,
              productEntity.getId(),
              main.get(i),
              ImageType.MAIN,
              i + 1);
        }
      }
      if (details != null) {
        for (int i = 0; i < details.size(); i++) {
          fileHandler.insertFile(
              itemPath,
              TableType.PRODUCT,
              productEntity.getId(),
              details.get(i),
              ImageType.DETAIL,
              i + 1);
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void updateProduct(Long productId, ProductDto productDto, MultipartFile thumbnail, List<MultipartFile> main,
      List<MultipartFile> details) {

    ProductEntity productEntity = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

    productEntity.setProductName(productDto.getProductName());
    productEntity.setPrice(productDto.getPrice());
    productEntity.setDescription(productDto.getDescription());
    productEntity.setProductType(productDto.getProductType());
    productEntity.setBillingType(productDto.getBillingType());
    productEntity.setProductStatus(productDto.getProductStatus());
    productEntity.setCategory(productDto.getCategory());
    productEntity.setDuration(productDto.getDuration());
    productEntity.setSessionCount(productDto.getSessionCount());

    productRepository.save(productEntity);
    try {
      fileHandler.insertFile(
          itemPath,
          TableType.PRODUCT,
          productEntity.getId(),
          thumbnail,
          ImageType.THUMBNAIL,
          1);

      if (main != null) {
        for (int i = 0; i < main.size(); i++) {
          fileHandler.insertFile(
              itemPath,
              TableType.PRODUCT,
              productEntity.getId(),
              main.get(i),
              ImageType.MAIN,
              i + 1);
        }
      }
      if (details != null) {
        for (int i = 0; i < details.size(); i++) {
          fileHandler.insertFile(
              itemPath,
              TableType.PRODUCT,
              productEntity.getId(),
              details.get(i),
              ImageType.DETAIL,
              i + 1);
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void deleteProduct(Long productId) {
    try {
      fileHandler.deleteProductFiles(itemPath, productId);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    productRepository.deleteById(productId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductDto> productList(ProductType productType, Pageable pageable, String search) {

    Page<ProductEntity> page;

    if (productType == null) {
      page = productRepository.findAll(pageable);
    } else {
      page = productRepository.findByProductType(productType, pageable);
    }

    return page.map(ProductDto::toProductDto);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductDto productDetail(Long productId) {

    ProductEntity productEntity = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

    return ProductDto.toProductDto(productEntity);
  }

  @Override
  public void deleteImage(Long productFileId) {

    try {
      fileHandler.deleteSingleFile(productFileId, itemPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Transactional
  public void deleteImages(Long productId) {
    try {
      fileHandler.deleteProductFiles(itemPath, productId);

      ProductEntity productEntity = productRepository.findById(productId)
          .orElseThrow(() -> new NoSuchElementException("상품이 없습니다."));
      productEntity.getFileEntities().clear();

    } catch (IOException e) {
      throw new RuntimeException("상품 이미지 전체 삭제 실패", e);
    }
  }

  public ProductDto getPremiumProduct() {

    ProductEntity product = productRepository
        .findFirstByProductType(ProductType.PREMIUM)
        .orElseThrow(
            () -> new RuntimeException("프리미엄 상품 없음"));

    return ProductDto.toProductDto(product);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ProductDto> getTopSalesProducts() {

    Pageable pageable = PageRequest.of(0, 8);

    List<ProductEntity> topProducts = new ArrayList<>(productRepository.findTopSalesProducts(pageable));

    System.out.println("판매상품 : " + topProducts.size());

    if (topProducts.size() < 8) {

      int needCount = 8 - topProducts.size();

      Pageable fallbackPageable = PageRequest.of(0, needCount);

      List<ProductEntity> latestProducts = new ArrayList<>(productRepository.findAllByOrderByIdDesc(fallbackPageable));

      System.out.println("최신상품 : " + latestProducts.size());

      for (ProductEntity product : latestProducts) {

        if (topProducts.size() < 8) {
          topProducts.add(product);
        }
      }
    }

    System.out.println("최종상품 : " + topProducts.size());

    return topProducts.stream()
        .map(ProductDto::toProductDto)
        .toList();
  }
}