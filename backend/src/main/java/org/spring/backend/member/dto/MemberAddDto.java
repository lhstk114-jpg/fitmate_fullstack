package org.spring.backend.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spring.backend.member.enumtype.Interest;
import org.spring.backend.member.entity.MemberAddEntity;
import org.spring.backend.member.entity.MemberEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAddDto {
    private Long id;

    private float height;

    private float weight;

    private float goalWeight;

    private int dailyCheck;

    private Interest interest;

    private String badge;

    private MemberEntity memberEntity;

    private Long memberId;

    public static MemberAddDto toMemberAddDto(MemberAddEntity memberAddEntity){
        return MemberAddDto.builder()
                .id(memberAddEntity.getId())
                .height(memberAddEntity.getHeight())
                .weight(memberAddEntity.getWeight())
                .goalWeight(memberAddEntity.getGoalWeight())
                .interest(memberAddEntity.getInterest())
                .memberEntity(memberAddEntity.getMemberEntity())
                .memberId(memberAddEntity.getMemberEntity().getId())
                .build();
    }
}
