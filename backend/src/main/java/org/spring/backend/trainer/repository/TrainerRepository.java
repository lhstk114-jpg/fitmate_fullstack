package org.spring.backend.trainer.repository;

import java.util.Optional;

import org.spring.backend.trainer.entity.TrainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<TrainerEntity, Long> {

  Optional<TrainerEntity> findByMemberId(Long memberId);

}
