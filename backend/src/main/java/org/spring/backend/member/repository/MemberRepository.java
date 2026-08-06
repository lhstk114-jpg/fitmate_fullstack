package org.spring.backend.member.repository;

import org.spring.backend.member.enumtype.Role;
import org.spring.backend.member.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUserEmail(String userEmail);

    boolean existsByUserEmail(String userEmail);

    Page<MemberEntity> findByRoleAndUserNameContaining(Pageable pageable, String search, Role role);

    Page<MemberEntity> findByRoleAndUserEmailContaining(Pageable pageable, String search, Role role);

    //멤버 AddEntity까지 불러올수있게 설정
    @Override
    @EntityGraph(attributePaths = {"memberAddEntity"})
    Page<MemberEntity> findAll(Pageable pageable);

    Page<MemberEntity> findByRoleContaining(Pageable pageable, String search);

    //memberAddEntity에 1:1로 매칭되어 저장된 관심사 가져오는 함수
    @Query(
            value = "SELECT m FROM MemberEntity m " +
                    "INNER JOIN FETCH m.memberAddEntity a " +
                    "WHERE m.role = :role AND a.interest LIKE CONCAT('%', :search, '%')",
            countQuery = "SELECT count(m) FROM MemberEntity m " +
                    "INNER JOIN m.memberAddEntity a " +
                    "WHERE m.role = :role AND a.interest LIKE CONCAT('%', :search, '%')"
    )
    Page<MemberEntity> findByRoleAndInterest(
            Pageable pageable,
            @Param("search") String search,
            @Param("role") Role role
    );

    @Query(value = "SELECT m FROM MemberEntity m " +
            "INNER JOIN FETCH m.memberAddEntity a " +
            "WHERE a.interest LIKE CONCAT('%', :search, '%')",
            //기존 JPA문처럼 count(총합 수)를 가져올수 없기때문에 따로 쿼리문으로 불러옴
            countQuery = "SELECT count(m) FROM MemberEntity m " +
                    "INNER JOIN m.memberAddEntity a " +
                    "WHERE a.interest LIKE CONCAT('%', :search, '%')")
    Page<MemberEntity> findByInterest(Pageable pageable, @Param("search") String search);

    @Query(value = "SELECT m FROM MemberEntity m " +
            "INNER JOIN FETCH m.memberAddEntity a " +
            "WHERE a.interest LIKE CONCAT('%', :search, '%')",
            //기존 JPA문처럼 count(총합 수)를 가져올수 없기때문에 따로 쿼리문으로 불러옴
            countQuery = "SELECT count(m) FROM MemberEntity m " +
                    "INNER JOIN m.memberAddEntity a " +
                    "WHERE a.interest LIKE CONCAT('%', :search, '%')")
    Page<MemberEntity> findByUserNameContaining(Pageable pageable, String search);

    //구독 유지중인 회원 수
    Long countByRoleAndSubscribe(Role role, Integer subscribe);

    //Role에 따른 회원 수
    Long countByRole(Role role);

    Page<MemberEntity> findByRole(Pageable pageable, Role role);
}
