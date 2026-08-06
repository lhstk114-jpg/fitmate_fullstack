import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import "../../css/admin/Admin.css";
import jwtAxios from "../../apis/util/jwtUtil";
import { API_SERVER_URL } from "../../apis/commonApi";
//recharts import
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
} from "recharts";

/* ==================== 차트 색상 ==================== */
// 구독 회원 / 미구독 회원
const SUBSCRIPTION_COLORS = ["#4f8ef7", "#ff6b6b"];
// 관심사 막대그래프
const INTEREST_COLORS = ["#4f8ef7", "#22c55e", "#f59e0b", "#ef4444", "#8b5cf6"];
// 게시글 선 그래프
const COMMUNITY_LINE_COLOR = "#36a269";
// 매출 선 그래프
const SALES_LINE_COLOR = "#f59e0b";
/* ==================== 관심사 한글 변환 ==================== */
const INTEREST_LABELS = {
  DIET: "다이어트",
  WORKOUT: "운동",
  HEALTH: "건강",
  UNREGISTERED: "관심사 미등록",
};
const AdminIndex = () => {
  /* ==================== 대시보드 데이터 ==================== */
  const [dashboardData, setDashboardData] = useState({
    // 주요 현황
    summary: {
      totalMemberCount: 0,
      totalProductCount: 0,
      todayCommunityCount: 0,
      todaySales: 0,
      currentMonthSales: 0,
      lastMonthSales: 0,
    },
    // 회원 CRM
    member: {
      activeSubscriptionCount: 0,
      expiringSubscriptionCount: 0,
      expiredSubscriptionCount: 0,
      unsubscribedMemberCount: 0,
      subscriptionRate: 0,
      interestRegistrationRate: 0,
    },
    // 차트 데이터
    interestChart: [],
    communityChart: [],
    salesChart: [],
    subscriptionChart: [],
    // TOP5 목록
    communityList: [],
    productList: [],
  });

  /* ==================== 대시보드 조회 ==================== */
  const getDashboardData = async () => {
    try {
      const res = await jwtAxios.get(`${API_SERVER_URL}/api/admin/dashboard`);
      setDashboardData({
        summary: res.data.summary || {
          totalMemberCount: 0,
          totalProductCount: 0,
          todayCommunityCount: 0,
          todaySales: 0,
          currentMonthSales: 0,
          lastMonthSales: 0,
        },
        member: res.data.member || {
          activeSubscriptionCount: 0,
          expiringSubscriptionCount: 0,
          expiredSubscriptionCount: 0,
          unsubscribedMemberCount: 0,
          subscriptionRate: 0,
          interestRegistrationRate: 0,
        },
        interestChart: res.data.interestChart || [],
        communityChart: res.data.communityChart || [],
        salesChart: res.data.salesChart || [],
        subscriptionChart: res.data.subscriptionChart || [],

        communityList: res.data.communityList || [],
        productList: res.data.productList || [],
      });
    } catch (err) {
      console.error("대시보드 조회 실패", err);
    }
  };

  useEffect(() => {
    getDashboardData();
  }, []);

  //데이터 구조 분해
  const {
    summary,
    member,
    interestChart,
    communityChart,
    salesChart,
    subscriptionChart,
    communityList,
    productList,
  } = dashboardData;

  return (
    <div className="admin-main">
      <main className="adminIndex">
        <div className="adminIndex-wrap">
          {/* ==================== 페이지 상단 ==================== */}
          <div className="adminIndex-top">
            <div className="adminIndex-top-con">
              <div className="title">
                <h1>관리자 대시보드</h1>
                <p>주요 운영 현황</p>
              </div>
            </div>
          </div>
          {/* ==================== 주요 현황 ==================== */}
          <section className="adminIndex-dashboard-wrap">
            <div className="title">
              <h2>주요 현황</h2>
            </div>
            <div className="adminIndex-summary-list">
              {/* 전체 회원 */}
              <Link to="/admin/member" className="adminIndex-summary-card">
                <span>전체 회원</span>
                <strong>
                  {(summary.totalMemberCount ?? 0).toLocaleString()}명
                </strong>
              </Link>
              {/* 전체 상품 */}
              <Link to="/admin/product" className="adminIndex-summary-card">
                <span>전체 상품</span>
                <strong>
                  {(summary.totalProductCount ?? 0).toLocaleString()}개
                </strong>
              </Link>
              {/* 오늘 게시글 */}
              <Link to="/admin/community" className="adminIndex-summary-card">
                <span>오늘 게시글</span>
                <strong>
                  {(summary.todayCommunityCount ?? 0).toLocaleString()}건
                </strong>
              </Link>
              {/* 오늘 매출 */}
              <Link to="/admin/order" className="adminIndex-summary-card">
                <span>오늘 매출</span>
                <strong>{(summary.todaySales ?? 0).toLocaleString()}원</strong>
              </Link>
              {/* 이번 달 누적 매출 */}
              <Link to="/admin/order" className="adminIndex-summary-card">
                <span>이번 달 누적 매출</span>
                <strong>
                  {(summary.currentMonthSales ?? 0).toLocaleString()}원
                </strong>
              </Link>
              {/* 지난 달 매출 */}
              <Link to="/admin/order" className="adminIndex-summary-card">
                <span>지난 달 매출</span>
                <strong>
                  {(summary.lastMonthSales ?? 0).toLocaleString()}원
                </strong>
              </Link>
            </div>
          </section>

          {/* ==================== 회원 CRM ==================== */}
          <section className="adminIndex-member-wrap">
            <div className="title">
              <h2>회원 CRM</h2>
            </div>

            <div className="adminIndex-member-con">
              {/* 회원 및 구독 현황 */}
              <div className="adminIndex-card adminIndex-member-left-con">
                <div className="adminIndex-card-header">
                  <h3>회원 및 구독 현황</h3>
                </div>

                <ul>
                  <li>
                    <span>구독 유지 회원</span>
                    <strong>
                      {(member.activeSubscriptionCount ?? 0).toLocaleString()}명
                    </strong>
                  </li>
                  <li>
                    <span>미구독 회원</span>
                    <strong>
                      {(member.unsubscribedMemberCount ?? 0).toLocaleString()}명
                    </strong>
                  </li>
                  <li>
                    <span>구독률</span>
                    <strong>
                      {(member.subscriptionRate ?? 0).toLocaleString()}%
                    </strong>
                  </li>
                  <li>
                    <span>관심사 등록률</span>
                    <strong>
                      {(member.interestRegistrationRate ?? 0).toLocaleString()}%
                    </strong>
                  </li>
                </ul>
              </div>

              {/* 회원 분포 차트 */}
              <div className="adminIndex-card adminIndex-member-right-con">
                <div className="adminIndex-member-chart-list">
                  {/* 회원 구독 현황 */}
                  <div className="adminIndex-member-chart-item">
                    <h4>회원 구독 현황</h4>

                    {subscriptionChart.length > 0 ? (
                      <div className="adminIndex-chart">
                        <ResponsiveContainer width="100%" height={250}>
                          <PieChart>
                            <Pie
                              data={subscriptionChart}
                              dataKey="value"
                              nameKey="label"
                              cx="50%"
                              cy="50%"
                              innerRadius={45}
                              outerRadius={75}
                              paddingAngle={3}
                              label={({ name, percent }) =>
                                `${name} ${(percent * 100).toFixed(1)}%`
                              }
                            >
                              {subscriptionChart.map((chart, index) => (
                                <Cell
                                  key={chart.label}
                                  fill={
                                    SUBSCRIPTION_COLORS[
                                      index % SUBSCRIPTION_COLORS.length
                                    ]
                                  }
                                />
                              ))}
                            </Pie>

                            <Tooltip
                              formatter={(value) => [
                                `${value.toLocaleString()}명`,
                                "회원 수",
                              ]}
                            />

                            <Legend />
                          </PieChart>
                        </ResponsiveContainer>
                      </div>
                    ) : (
                      <div className="adminIndex-empty">
                        회원 구독 데이터가 없습니다.
                      </div>
                    )}
                  </div>

                  {/* 관심사별 회원 분포 */}
                  <div className="adminIndex-member-chart-item">
                    <h4>관심사별 회원 분포</h4>

                    {interestChart.length > 0 ? (
                      <div className="adminIndex-chart">
                        <ResponsiveContainer width="100%" height={250}>
                          <PieChart>
                            <Pie
                              data={interestChart}
                              dataKey="value"
                              nameKey="label"
                              cx="50%"
                              cy="50%"
                              innerRadius={45}
                              outerRadius={75}
                              paddingAngle={3}
                              label={({ name, percent }) =>
                                `${INTEREST_LABELS[name] || name} ${(
                                  percent * 100
                                ).toFixed(1)}%`
                              }
                            >
                              {interestChart.map((chart, index) => (
                                <Cell
                                  key={chart.label}
                                  fill={
                                    INTEREST_COLORS[
                                      index % INTEREST_COLORS.length
                                    ]
                                  }
                                />
                              ))}
                            </Pie>

                            <Tooltip
                              formatter={(value, name) => [
                                `${value.toLocaleString()}명`,
                                INTEREST_LABELS[name] || name,
                              ]}
                            />

                            <Legend
                              formatter={(value) =>
                                INTEREST_LABELS[value] || value
                              }
                            />
                          </PieChart>
                        </ResponsiveContainer>
                      </div>
                    ) : (
                      <div className="adminIndex-empty">
                        관심사 데이터가 없습니다.
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </section>

          {/* ==================== 커뮤니티 ==================== */}

          <section className="adminIndex-community-wrap">
            <div className="title">
              <h2>커뮤니티</h2>
            </div>

            <div className="adminIndex-community-con">
              {/* 조회수 TOP5 */}
              <div className="adminIndex-card adminIndex-community-left-con">
                <div className="adminIndex-card-header">
                  <h3>조회수 TOP 5</h3>
                </div>
                {communityList.length > 0 ? (
                  <ul>
                    {communityList.map((community, index) => (
                      <li key={community.id}>
                        <span className="rank">{index + 1}</span>

                        <Link
                          to={`/admin/community/detail/${community.id}`}
                          className="item-title"
                        >
                          {community.title}
                        </Link>

                        <span className="item-count">
                          조회 {(community.hit ?? 0).toLocaleString()}
                        </span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div className="adminIndex-empty">
                    등록된 게시글이 없습니다.
                  </div>
                )}
              </div>

              {/* 최근 7일 게시글 등록 추이 */}
              <div className="adminIndex-card adminIndex-community-right-con">
                <h3>최근 7일 게시글 등록 추이</h3>

                {communityChart.length > 0 ? (
                  <div className="adminIndex-chart">
                    <ResponsiveContainer width="100%" height={300}>
                      <LineChart
                        data={communityChart}
                        margin={{
                          top: 20,
                          right: 25,
                          left: 5,
                          bottom: 10,
                        }}
                      >
                        <CartesianGrid strokeDasharray="3 3" />

                        <XAxis
                          dataKey="label"
                          tickFormatter={(value) => value.substring(5)}
                          tickLine={false}
                          axisLine={false}
                        />

                        <YAxis
                          allowDecimals={false}
                          tickLine={false}
                          axisLine={false}
                        />

                        <Tooltip
                          labelFormatter={(label) => label.substring(5)}
                          formatter={(value) => [
                            `${value.toLocaleString()}건`,
                            "게시글 수",
                          ]}
                        />

                        <Line
                          type="monotone"
                          dataKey="value"
                          name="게시글 수"
                          stroke={COMMUNITY_LINE_COLOR}
                          strokeWidth={3}
                          activeDot={{ r: 6 }}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                ) : (
                  <div className="adminIndex-empty">
                    게시글 추이 데이터가 없습니다.
                  </div>
                )}
              </div>
            </div>
          </section>
          {/* ==================== 상품 및 매출 ==================== */}
          <section className="adminIndex-product-wrap">
            <div className="title">
              <h2>상품 및 매출</h2>
            </div>
            <div className="adminIndex-product-con">
              {/* 상품 판매량 TOP5 */}
              <div className="adminIndex-card adminIndex-product-left-con">
                <div className="adminIndex-card-header">
                  <h3>상품 판매량 TOP 5</h3>
                </div>

                {productList.length > 0 ? (
                  <ul>
                    {productList.map((product, index) => (
                      <li key={product.id}>
                        <span className="rank">{index + 1}</span>

                        <Link
                          to={`/products/detail/${product.id}`}
                          className="item-title"
                        >
                          {product.productName}
                        </Link>

                        <span className="item-count">
                          {(product.salesCount ?? 0).toLocaleString()}개
                        </span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <div className="adminIndex-empty">
                    판매된 상품이 없습니다.
                  </div>
                )}
              </div>

              {/* 최근 7일 매출 추이 */}
              <div className="adminIndex-card adminIndex-product-right-con">
                <h3>최근 7일 매출 추이</h3>

                {salesChart.length > 0 ? (
                  <div className="adminIndex-chart">
                    <ResponsiveContainer width="100%" height={300}>
                      <LineChart
                        data={salesChart}
                        margin={{
                          top: 20,
                          right: 25,
                          left: 15,
                          bottom: 10,
                        }}
                      >
                        <CartesianGrid strokeDasharray="3 3" />

                        <XAxis
                          dataKey="label"
                          tickFormatter={(value) => value.substring(5)}
                          tickLine={false}
                          axisLine={false}
                        />

                        <YAxis
                          tickFormatter={(value) => value.toLocaleString()}
                          tickLine={false}
                          axisLine={false}
                        />

                        <Tooltip
                          labelFormatter={(label) => label.substring(5)}
                          formatter={(value) => [
                            `${value.toLocaleString()}원`,
                            "매출",
                          ]}
                        />

                        <Line
                          type="monotone"
                          dataKey="value"
                          name="매출"
                          stroke={SALES_LINE_COLOR}
                          strokeWidth={3}
                          activeDot={{ r: 6 }}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                ) : (
                  <div className="adminIndex-empty">
                    매출 추이 데이터가 없습니다.
                  </div>
                )}
              </div>
            </div>
          </section>

          <div className="adminIndex-bottom">
            <div className="adminIndex-bottom-con"></div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminIndex;
