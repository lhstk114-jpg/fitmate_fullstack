package org.spring.backend.member.repository;

import org.spring.backend.member.entity.MemberAddEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberAddRepository extends JpaRepository<MemberAddEntity, Long> {
}
