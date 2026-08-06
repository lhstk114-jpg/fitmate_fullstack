package org.spring.backend.trainer.dto;

import org.spring.backend.trainer.entity.TrainerEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerDto {

    private Long id;

    private Long memberId;

    private String name; // 회원 이름

    private String career; // 경력

    private String specialty; // 전문 분야

    private String introduce; // 자기소개

    private String certificate; // 자격증

    private String profileImage; // 프로필 이미지


    public static TrainerDto toTrainerDto(TrainerEntity trainer) {

        return TrainerDto.builder()
                .id(trainer.getId())
                .memberId(trainer.getMember().getId())
                .name(trainer.getMember().getUserName())
                .career(trainer.getCareer())
                .specialty(trainer.getSpecialty())
                .introduce(trainer.getIntroduce())
                .certificate(trainer.getCertificate())
                .profileImage(
                        trainer.getMember().getFileEntities() != null &&
                                !trainer.getMember().getFileEntities().isEmpty()
                                ? trainer.getMember().getFileEntities().get(0).getNewFileName()
                                : null
                ).build();
    }
}