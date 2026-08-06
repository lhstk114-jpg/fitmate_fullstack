package org.spring.backend.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.spring.backend.admin.popup.dto.PopupDto;
import org.spring.backend.admin.popup.service.PopupService;
import org.spring.backend.community.dto.CommunityDto;
import org.spring.backend.community.repository.CommunityRepository;
import org.spring.backend.main.dto.MainResponseDto;
import org.spring.backend.main.service.MainService;
import org.spring.backend.member.enumtype.Interest;
import org.spring.backend.shop.order.repository.OrderItemRepository;
import org.spring.backend.shop.product.dto.ProductDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainServiceImpl implements MainService {

    private final CommunityRepository communityRepository;
    private final OrderItemRepository orderItemRepository;
    private final PopupService popupService;

    private static final int MAIN_PRODUCT_LIMIT = 8;
    private static final int RECOMMENDED_PRODUCT_LIMIT = 4;

    // ============================================================
    // 비로그인 사용자용 메인 데이터
    // ============================================================
    @Override
    public MainResponseDto getDefaultMainData() {

        // 공지사항 최신순 TOP 5
        List<CommunityDto> noticeList =
                communityRepository
                        .findTop5ByTabNameOrderByCreateTimeDesc("공지사항")
                        .stream()
                        .map(entity -> CommunityDto.builder()
                                .id(entity.getId())
                                .title(entity.getTitle())
                                .build())
                        .toList();

        // 공지사항 제외 조회수 높은 게시글 TOP 5
        List<CommunityDto> communityList =
                communityRepository
                        .findTop5ByTabNameNotOrderByHitDesc("공지사항")
                        .stream()
                        .map(entity -> CommunityDto.builder()
                                .id(entity.getId())
                                .title(entity.getTitle())
                                .build())
                        .toList();

        // 비회원은 전체 인기 상품 TOP 8
        Pageable popularPageable =
                PageRequest.of(0, MAIN_PRODUCT_LIMIT);

        List<ProductDto> productList =
                orderItemRepository
                        .findPopularProducts(popularPageable)
                        .stream()
                        .map(ProductDto::toProductDto)
                        .limit(MAIN_PRODUCT_LIMIT)
                        .toList();

        // 현재 노출 가능한 팝업
        List<PopupDto> popupList =
                popupService.getActivePopupList();

        return MainResponseDto.builder()
                .noticeList(noticeList)
                .communityList(communityList)
                .productList(productList)
                .popupList(popupList)
                .build();
    }

    // ============================================================
    // 로그인 사용자 관심사 기반 메인 데이터
    // ============================================================
    @Override
    public MainResponseDto getMainData(Interest interest) {

        String productCategory = interest.getProductCategory();
        String communityTabName = interest.getCommunityTabName();

        // 공지사항 최신순 TOP 5
        List<CommunityDto> noticeList =
                communityRepository
                        .findTop5ByTabNameOrderByCreateTimeDesc("공지사항")
                        .stream()
                        .map(entity -> CommunityDto.builder()
                                .id(entity.getId())
                                .title(entity.getTitle())
                                .build())
                        .toList();

        // 관심 게시판 조회수 높은 게시글 TOP 5
        List<CommunityDto> communityList =
                communityRepository
                        .findTop5ByTabNameOrderByHitDesc(communityTabName)
                        .stream()
                        .map(entity -> CommunityDto.builder()
                                .id(entity.getId())
                                .title(entity.getTitle())
                                .build())
                        .toList();

        /*
         * 1. 관심 카테고리 추천 상품 최대 4개
         */
        Pageable recommendedPageable =
                PageRequest.of(0, RECOMMENDED_PRODUCT_LIMIT);

        List<ProductDto> recommendedProducts =
                orderItemRepository
                        .findPopularProductsByCategory(
                                productCategory,
                                recommendedPageable
                        )
                        .stream()
                        .map(ProductDto::toProductDto)
                        .toList();

        /*
         * 2. 전체 인기 상품 조회
         *
         * 추천 상품과 중복될 수 있으므로 8개보다 넉넉하게 조회합니다.
         */
        Pageable popularPageable =
                PageRequest.of(0, MAIN_PRODUCT_LIMIT * 2);

        List<ProductDto> popularProducts =
                orderItemRepository
                        .findPopularProducts(popularPageable)
                        .stream()
                        .map(ProductDto::toProductDto)
                        .toList();

        /*
         * 3. 추천 상품을 먼저 넣고
         * 전체 인기 상품으로 부족한 수량을 채웁니다.
         */
        List<ProductDto> productList =
                mergeMainProducts(
                        recommendedProducts,
                        popularProducts
                );

        // 현재 노출 가능한 팝업
        List<PopupDto> popupList =
                popupService.getActivePopupList();

        return MainResponseDto.builder()
                .noticeList(noticeList)
                .communityList(communityList)
                .productList(productList)
                .popupList(popupList)
                .build();
    }

    // ============================================================
    // 메인 상품 8개 조합
    // ============================================================
    private List<ProductDto> mergeMainProducts(
            List<ProductDto> recommendedProducts,
            List<ProductDto> popularProducts
    ) {

        List<ProductDto> result = new ArrayList<>();
        Set<Long> addedProductIds = new HashSet<>();

        /*
         * 추천 상품 최대 4개 추가
         */
        for (ProductDto product : recommendedProducts) {

            if (result.size() >= RECOMMENDED_PRODUCT_LIMIT) {
                break;
            }

            addProductIfAbsent(
                    result,
                    addedProductIds,
                    product
            );
        }

        /*
         * 인기 상품으로 총 8개까지 채우기
         */
        for (ProductDto product : popularProducts) {

            if (result.size() >= MAIN_PRODUCT_LIMIT) {
                break;
            }

            addProductIfAbsent(
                    result,
                    addedProductIds,
                    product
            );
        }

        return result;
    }

    // ============================================================
    // 상품 중복 확인 후 추가
    // ============================================================
    private void addProductIfAbsent(
            List<ProductDto> result,
            Set<Long> addedProductIds,
            ProductDto product
    ) {

        if (product == null || product.getId() == null) {
            return;
        }

        if (addedProductIds.contains(product.getId())) {
            return;
        }

        result.add(product);
        addedProductIds.add(product.getId());
    }

    // ============================================================
    // 게시판 탭별 베스트 게시글
    // ============================================================
    @Override
    public List<CommunityDto> getBestCommunityList(String tabName) {

        return communityRepository
                .findTop5ByTabNameOrderByHitDesc(tabName)
                .stream()
                .map(entity -> CommunityDto.builder()
                        .id(entity.getId())
                        .title(entity.getTitle())
                        .build())
                .toList();
    }
}