package org.spring.backend.member.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.spring.backend.member.enumtype.Interest;
import org.spring.backend.member.enumtype.Role;
import org.spring.backend.member.entity.MemberAddEntity;
import org.spring.backend.member.entity.MemberEntity;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
  private Long id;

  private String userEmail;

  private String userPw;

  private String userName;

  private String userAddress;

  private String userPhone;

  private int subscribe;

  private int profilePhoto;

  private Role role;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;

  private MultipartFile memberFile; //실제 파일

  private String newFileName; //새이름 -> DB, 로컬 저장 이름

  private String oldFileName;//원본이름

  private Long memberAddId;

  //memberAdd의 요소들
  private Float height;

  private Float weight;

  private Float goalWeight;

  private Integer dailyCheck;

  private Interest interest;

  private String badge;

  public Boolean hasAdditionalData(){
    return interest != null || height != null || weight != null ||
            goalWeight != null || dailyCheck != null || badge != null;
  }


  public static MemberDto toMemberDto(MemberEntity memberEntity){
    MemberAddEntity addEntity = memberEntity.getMemberAddEntity();
    return MemberDto.builder()
            .id(memberEntity.getId())
            .userEmail(memberEntity.getUserEmail())
            .userPw(memberEntity.getUserPw())
            .userName(memberEntity.getUserName())
            .userAddress(memberEntity.getUserAddress())
            .userPhone(memberEntity.getUserPhone())
            .subscribe(memberEntity.getSubscribe())
            .profilePhoto(memberEntity.getProfilePhoto())
            .role(memberEntity.getRole())
            .createTime(memberEntity.getCreateTime())
            .updateTime(memberEntity.getUpdateTime())
            .memberAddId(memberEntity.getMemberAddEntity().getId())
            //memberAddEntity의 값들 저장
            .interest(addEntity != null ? addEntity.getInterest() : null)
            .height(addEntity != null ? addEntity.getHeight() : null)
            .weight(addEntity != null ? addEntity.getWeight() : null)
            .goalWeight(addEntity != null ? addEntity.getGoalWeight() : null)
            // 파일 엔티티가 존재할 때만 이름을 넣고, 없으면 null 세팅
            .newFileName(memberEntity.getFileEntities() != null && !memberEntity.getFileEntities().isEmpty() ? memberEntity.getFileEntities().get(0).getNewFileName() : null)
            .oldFileName(memberEntity.getFileEntities() != null && !memberEntity.getFileEntities().isEmpty() ? memberEntity.getFileEntities().get(0).getOldFileName() : null)
            .build();
  }

  public static MemberDto toMemberDtoSummary(MemberEntity memberEntity){
    MemberAddEntity addEntity = memberEntity.getMemberAddEntity();
    return MemberDto.builder()
            .id(memberEntity.getId())
            .userEmail(memberEntity.getUserEmail())
            .userName(memberEntity.getUserName())
            .userPhone(memberEntity.getUserPhone())
            .role(memberEntity.getRole())
            //memberAddEntity의 값들 저장
            .interest(addEntity != null ? addEntity.getInterest() : null)
            .height(addEntity != null ? addEntity.getHeight() : null)
            .weight(addEntity != null ? addEntity.getWeight() : null)
            .goalWeight(addEntity != null ? addEntity.getGoalWeight() : null)
            // 파일 엔티티가 존재할 때만 이름을 넣고, 없으면 null 세팅
            .newFileName(memberEntity.getFileEntities() != null && !memberEntity.getFileEntities().isEmpty() ? memberEntity.getFileEntities().get(0).getNewFileName() : null)
            .oldFileName(memberEntity.getFileEntities() != null && !memberEntity.getFileEntities().isEmpty() ? memberEntity.getFileEntities().get(0).getOldFileName() : null)
            .build();
  }

  public static MemberDto toInitMemberDto(MemberEntity memberEntity){
    return MemberDto.builder()
            .userEmail(memberEntity.getUserEmail())
            .userName(memberEntity.getUserName())
            .subscribe(memberEntity.getSubscribe())
            .role(memberEntity.getRole())
            .memberAddId(memberEntity.getMemberAddEntity().getId())
            .build();
  }
}
