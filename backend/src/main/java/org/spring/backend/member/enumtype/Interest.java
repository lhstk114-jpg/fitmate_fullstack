package org.spring.backend.member.enumtype;

    public enum Interest {
        //Interest Enum 구분
        DIET,
        WORKOUT,
        HEALTH;

        //product 카테고리
        public String getProductCategory() {
            return switch (this) {
                case DIET -> "식품";
                case WORKOUT -> "운동기구";
                case HEALTH -> "식품";
            };
        }

        //community 카테고리
        public String getCommunityTabName() {
            return switch (this) {
                case DIET -> "자유게시판";
                case WORKOUT -> "운동게시판";
                case HEALTH -> "운동게시판";
            };
        }
    }