package org.spring.backend.member.repository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import org.spring.backend.member.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {
    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);

    @Modifying
    @Transactional
    void deleteByUserEmail(String userEmail);

    List<RefreshEntity> findAllByUserEmail(String userEmail);
}
