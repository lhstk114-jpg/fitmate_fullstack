package org.spring.backend.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.spring.backend.common.BasicTime;
import org.spring.backend.member.enumtype.Interest;
import org.spring.backend.member.dto.MemberAddDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "member_add")
public class MemberAddEntity extends BasicTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_add_id")
    private Long id;

    //유저 관심사
    @Enumerated(EnumType.STRING)
    @Column(name = "interest")
    private Interest interest;

    private float height;

    private float weight;

    private float goalWeight;

    //1:1매칭관계
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "memberAddEntity")
    private MemberEntity memberEntity;

    public static MemberAddEntity createDefault() {
        return MemberAddEntity.builder()
                .height(0)
                .weight(0)
                .goalWeight(0)
                .build();
    }

    public static MemberAddEntity toUpdateMemberAddEntity(MemberAddDto memberAddDto){
        return MemberAddEntity.builder()
                .id(memberAddDto.getId())
                .height(0)
                .weight(0)
                .goalWeight(0)
                .memberEntity(memberAddDto.getMemberEntity())
                .build();
    }
}
