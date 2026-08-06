package org.spring.backend.community.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.spring.backend.weather.service.WeatherService;
import org.spring.backend.community.dto.CategoryDto;
import org.spring.backend.community.dto.CommunityDto;
import org.spring.backend.community.dto.TabDto;
import org.spring.backend.community.service.CommunityService;
import org.spring.backend.community.service.TabService;
import org.spring.backend.member.jwt.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 커뮤니티(게시글/탭/카테고리/날씨) 관련 일반 사용자 + 공용 API
 * - 게시글 CRUD, 탭/카테고리 조회, 메인 페이지 데이터, 지역별 날씨까지 한 컨트롤러에서 처리
 * - 프론트의 CommunityMain, CommunityList, CommunityDetail, CommunityInsert, CommunityUpdate,
 *   AdminCommunity, TabList 등 여러 페이지가 이 컨트롤러를 호출함
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community")
@Slf4j
public class CommunityController {

  private final CommunityService communityService;
  private final TabService tabService;
  private final WeatherService weatherService;

  /**
   * 커뮤니티 메인 페이지 데이터 조회
   * 프론트: CommunityMain.jsx → GET /community/main (또는 "", "/")
   * 탭별 미리보기 목록(byTab), 전체 추천글(all), 탭 목록(tabs)을 한 번에 반환
   */
  @GetMapping({"","/","/main"})
  public ResponseEntity<?> mainList() {
    Map<String, Object> data = communityService.mainList();

    Map<String, Object> map = new HashMap<>();
    map.put("result", data);
    return ResponseEntity.status(HttpStatus.OK).body(map);
  }

  //게시글 리스트
  // 참고: 프론트에서는 이 API 대신 필터가 더 풍부한 /tclist를 주로 사용하는 것으로 보임
  @GetMapping("communityList")
  public ResponseEntity<?> communityList( @PageableDefault(size = 10, sort = "createTime", direction = Sort.Direction.DESC)Pageable pageable,
                                         @RequestParam(required = false) String subject,
                                         @RequestParam(required = false) String search){
    Map<String, Page<CommunityDto>> map = new HashMap<>();

    Page<CommunityDto> communityList = communityService.communityList(pageable, subject, search);
    map.put("result", communityList);

    return ResponseEntity.status(HttpStatus.OK).body(map);
  }

  /**
   * 게시글 작성
   * 프론트: CommunityInsert.jsx / AdminNoticeWrite.jsx → POST /community/insert
   * 로그인 여부를 서버에서도 재검증 (비로그인/익명 사용자는 401 반환)
   */
  //게시글 작성
  @PostMapping("/insert")
  public ResponseEntity<?> communityInsert(@RequestBody CommunityDto communityDto,Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요한 서비스입니다.");
    }
    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
    String userEmail = customUserDetails.getMemberEntity().getUserEmail();


    Map<String, CommunityDto> map = new HashMap<>();
    communityService.communityInsert(communityDto, userEmail);
    map.put("community", communityDto);
    return ResponseEntity.status(HttpStatus.OK).body(map);
  }

  /**
   * 게시글 삭제 (작성자 본인용)
   * 프론트: CommunityDetail.jsx의 getCommunityDelete → DELETE /community/delete/{id}
   * 서비스단(communityService.communityDelete)에서 요청자(userEmail)가 작성자 본인인지 검증
   */
  //게시글 삭제
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<?> communityDelete(@PathVariable("id") Long id,
                                           Authentication authentication){
    String userEmail = authentication.getName();
    communityService.communityDelete(id, userEmail);
    Map<String, String> map = new HashMap<>();
    map.put("result", "Delete");
      return ResponseEntity.status(HttpStatus.OK).body(map);
  }

  /**
   * 게시글 삭제 (관리자용)
   * 프론트: AdminCommunity.jsx / AdminCommunityDetail.jsx → DELETE /community/adminDelete/{id}
   * ✅ 수정: 기존에는 권한 검증 없이 바로 삭제했으나, Authentication에서 요청자 이메일을 꺼내
   *    communityService.adminDelete(id, userEmail)로 전달 → 서비스단에서 ADMIN 권한을 검증하도록 변경.
   *    (비로그인 요청은 userEmail이 없어 authentication.getName() 호출 시 예외가 발생하므로,
   *     이 경로는 반드시 인증된 사용자만 접근 가능해야 함 - Spring Security 설정에서
   *     "/community/adminDelete/**"가 인증 필요 경로로 등록되어 있는지 함께 확인 권장)
   */
  @DeleteMapping("/adminDelete/{id}")
  public ResponseEntity<?> adminDelete(@PathVariable("id") Long id, Authentication authentication){
    String userEmail = authentication.getName();
    communityService.adminDelete(id, userEmail);
    Map<String, String> map = new HashMap<>();
    map.put("result", "Delete");
    return ResponseEntity.status(HttpStatus.OK).body(map);
  }

  /**
   * 게시글 수정
   * 프론트: CommunityUpdate.jsx의 getCommunityUpdate → PUT /community/update/{id}
   * 서비스단에서 요청자(userEmail)가 작성자 본인인지 검증
   */
  //게시글 수정
  @PutMapping("/update/{id}")
  public ResponseEntity<?> communityUpdate(@PathVariable("id") Long id,
                                           @RequestBody CommunityDto communityDto,
                                           Authentication authentication){
    // 컨트롤러가 받은 id를 서비스로 확실하게 전달합니다.
    String userEmail = authentication.getName();
    communityService.communityUpdate(id, communityDto, userEmail);
    
    Map<String, CommunityDto> map = new HashMap<>();
    map.put("result", communityDto);
    return ResponseEntity.status(HttpStatus.OK).body(map);
}

  /**
   * 게시글 상세 조회
   * 프론트: CommunityDetail.jsx / AdminCommunityDetail.jsx → GET /community/detail/{id}?count=true
   * - count=true(게시글 상세)일 때만 조회수 증가, count=false(댓글 등 부가 조회)면 증가 안 함
   * - "postView{id}" 쿠키로 24시간 내 중복 조회수 증가를 방지
   * - 로그인 사용자면 customUserDetails에서 이메일을 꺼내 서비스에 전달
   */
  @GetMapping("/detail/{id}")
  public ResponseEntity<?> communityDetail(
          @PathVariable("id") Long id,
          @RequestParam(value = "count", defaultValue = "false") boolean count, // 명시적 선언
          @AuthenticationPrincipal CustomUserDetails customUserDetails,
          HttpServletRequest request,
          HttpServletResponse response) {

    // 게시글 상세에 true, 댓글에 false
    if (count) {
      // 쿠키 확인 로직: 이미 이 게시글을 조회한 적이 있는지 쿠키로 판별
      boolean isVisited = false;
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if (cookie.getName().equals("postView" + id)) {
            isVisited = true;
            break;
          }
        }
      }

      // 쿠키가 없으면 조회수 증가 및 쿠키 생성 (유효기간 24시간)
      if (!isVisited) {
        communityService.updateHit(id);
        Cookie newCookie = new Cookie("postView" + id, "visited");
        newCookie.setMaxAge(60 * 60 * 24);
        response.addCookie(newCookie);
      }
    }

    // 2. 데이터 조회 (비로그인 사용자는 userEmail이 null로 전달됨)
    String userEmail = (customUserDetails != null) ? customUserDetails.getUsername() : null;
    CommunityDto communityDto = communityService.communityDetail(id, userEmail);

    Map<String, CommunityDto> map = new HashMap<>();
    map.put("community", communityDto);
    return ResponseEntity.status(HttpStatus.OK).body(map);
  }

  /**
   * 전체 탭 목록 조회
   * 프론트: CommunityLeft.jsx, CommunityInsert.jsx, AdminCommunity.jsx 등 여러 곳에서 공통으로 사용
   */
  //탭 목록
  @GetMapping("tabList")
  public ResponseEntity<?> tabList(){
    Map<String, List<TabDto>> map = new HashMap<>();

    List<TabDto> tabDto = tabService.tabList();
    map.put("result", tabDto);

    return ResponseEntity.status(HttpStatus.OK).body(map);
  }

      /**
       * 탭 상세보기 (id로 특정 탭 하나만)
       * ✅ 수정: 기존에는 id 파라미터를 받고도 사용하지 않은 채 tabService.tabList()(전체 목록)를
       *    그대로 반환하고 있었음 → tabService.tabDetail(id)로 실제 해당 탭만 조회하도록 변경.
       *    응답 형태(List<TabDto>)는 프론트 호환을 위해 그대로 유지하고, 단일 탭을 리스트로 감싸서 반환.
       */
      //탭 상세보기 이동
      @GetMapping("tabList/{id}")
      public ResponseEntity<?> tabListDetail(@PathVariable("id") Long id){
        Map<String, List<TabDto>> map = new HashMap<>();

        TabDto tabDto = tabService.tabDetail(id);
        map.put("result", List.of(tabDto));

        return ResponseEntity.status(HttpStatus.OK).body(map);
      }

      /**
       * 전체 카테고리 목록 조회
       * 프론트: CommunityLeft.jsx, CommunityInsert.jsx, AdminCommunity.jsx 등에서
       * 이 전체 목록을 받아 tabId로 클라이언트단 필터링해서 사용
       */
      //카테고리 리스트 끌어오기
      @GetMapping("/category")
      public ResponseEntity<?> getCategoryList(){
        List<CategoryDto> categoryList = tabService.categoryList();
        Map<String , List<CategoryDto>> map = new HashMap<>();
        map.put("result", categoryList);
        return ResponseEntity.status(HttpStatus.OK).body(map);
      }

      /**
       * 탭/카테고리/키워드로 필터링된 게시글 목록 조회 (페이지네이션 포함)
       * 프론트: CommunityList.jsx, AdminCommunity.jsx의 fetchList/fetchGroupedList → GET /community/tclist
       * - tabId, categoryId, keyword 모두 선택적(optional) 파라미터 (없으면 전체 조회)
       * - AdminCommunity의 "탭별 그룹 모드"에서는 탭마다 이 API를 각각 별도 호출함 (size를 작게 줘서 미리보기용으로 사용)
       */
      @GetMapping("/tclist")
      public ResponseEntity<?> tcList(@RequestParam(value="tabId", required = false) Long tabId,
                                   @RequestParam(value = "categoryId", required = false) Long categoryId,
                                      @RequestParam(value="keyword", required = false) String keyword,
                                      @PageableDefault(size=10,sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable){
        Page<CommunityDto> page = communityService.findCommunityList(tabId, categoryId, keyword, pageable);

        Map<String, Object> map = new HashMap<>();
        map.put("result", page);
        return ResponseEntity.status(HttpStatus.OK).body(map);
      }


        /**
         * 지역별 날씨 조회 (프록시)
         * 프론트: CommunityMain.jsx → GET /community/weather?city={cityCode}
         * 실제 조회/캐싱 로직은 WeatherService에 위임 (2시간 TTL 캐시, 스케줄러가 1시간마다 선갱신)
         */
        @GetMapping("/weather")
        public ResponseEntity<?> getWeather(@RequestParam("city") String city) {
          Map<String, Object> weatherResult = weatherService.getWeather(city);

          Map<String, Object> map = new HashMap<>();
          map.put("result", weatherResult);
          return ResponseEntity.status(HttpStatus.OK).body(map);
        }
    }
