package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.Cluster;
import com.dsapkl.backend.entity.ClusterItemPreference;
import com.dsapkl.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClusterItemPreferenceRepository extends JpaRepository<ClusterItemPreference, Long> {
    Optional<ClusterItemPreference> findByClusterAndItem(Cluster cluster, Item item);
    
    // cluster_id와 cluster_number가 같은 데이터 중 선호도 높은 순으로 정렬
    @Query("SELECT cip FROM ClusterItemPreference cip " +
           "WHERE cip.cluster.id = :clusterId " +
           "AND cip.cluster.clusterNumber = " +
           "(SELECT c.clusterNumber FROM Cluster c WHERE c.id = :clusterId) " +
           "ORDER BY cip.preferenceScore DESC")
    List<ClusterItemPreference> findByClusterIdOrderByPreferenceScoreDesc(@Param("clusterId") Integer clusterId);
    
    // 위와 동일하지만 상위 N개만 조회
    @Query("SELECT cip FROM ClusterItemPreference cip " +
           "WHERE cip.cluster.id = :clusterId " +
           "AND cip.cluster.clusterNumber = " +
           "(SELECT c.clusterNumber FROM Cluster c WHERE c.id = :clusterId) " +
           "ORDER BY cip.preferenceScore DESC " +
           "LIMIT :limit")
    List<ClusterItemPreference> findTopNByClusterIdOrderByPreferenceScoreDesc(
            @Param("clusterId") Integer clusterId, 
            @Param("limit") int limit);
}