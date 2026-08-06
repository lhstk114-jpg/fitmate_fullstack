package org.spring.backend.calendar.repository;

import org.spring.backend.calendar.entity.PersonalScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonalScheduleRepository extends JpaRepository<PersonalScheduleEntity, Long> {
    List<PersonalScheduleEntity> findByMemberEntityIdAndEventType(Long memberId,String eventType);
}
