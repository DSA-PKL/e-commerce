package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.MemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {
    
   // memberId로 MemberInfo 찾기
//    Optional<MemberInfo> findByMemberId(Long memberId);
//
//    // Member 엔티티의 ID로 MemberInfo 존재 여부 확인
//    boolean existsByMemberId(Long memberId);
//
//    // 추가적인 쿼리 메소들...
//    @Query("SELECT mi FROM MemberInfo mi JOIN FETCH mi.member WHERE mi.memberId = :memberId")
//    Optional<MemberInfo> findByMemberIdWithMember(@Param("memberId") Long memberId);
    @Query("SELECT mi FROM MemberInfo mi WHERE mi.member.id = :memberId")
    Optional<MemberInfo> findByMemberId(@Param("memberId") Long memberId);

    // 모든 클러스터 번호 조회 (null 포함)
    @Query("SELECT DISTINCT m.cluster_id.clusterNumber FROM MemberInfo m")
    List<Integer> findDistinctClusterNumbers();

    // cluster_id가 null인 회원 조회
    @Query("SELECT m FROM MemberInfo m WHERE m.cluster_id IS NULL")
    List<MemberInfo> findMembersWithNullCluster();

    // 특정 cluster_id를 가진 회원 조회
    @Query("SELECT m FROM MemberInfo m WHERE m.cluster_id.clusterNumber = :clusterId")
    List<MemberInfo> findByClusterId(@Param("clusterId") Integer clusterId);
} 