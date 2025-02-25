package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    @Query("SELECT l FROM UserActivityLog l WHERE l.timestamp BETWEEN :start AND :end ORDER BY l.timestamp DESC")
    List<UserActivityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<UserActivityLog> findByUserIdOrderByTimestampDesc(Long userId);

    @Query("SELECT l.pageId, COUNT(l) FROM UserActivityLog l WHERE l.activityType = 'PAGE_VIEW' GROUP BY l.pageId")
    List<Object[]> getPageViewStats();

    @Query("SELECT l.searchKeyword, COUNT(l) FROM UserActivityLog l WHERE l.activityType = 'SEARCH' GROUP BY l.searchKeyword ORDER BY COUNT(l) DESC")
    List<Object[]> getPopularSearches();

    List<UserActivityLog> findByActivityTypeAndAdditionalDataContaining(String activityType, String content);
    List<UserActivityLog> findByActivityTypeAndTimestampBetween(String activityType, LocalDateTime start, LocalDateTime end);

    // 클릭 분석
    @Query("SELECT l.url, COUNT(l) FROM UserActivityLog l WHERE l.activityType = 'CLICK' GROUP BY l.url ORDER BY COUNT(l) DESC")
    List<Object[]> getMostClickedPages();

    // 세션 분석
    @Query("SELECT FUNCTION('HOUR', l.timestamp) as hour, COUNT(l) FROM UserActivityLog l WHERE l.activityType = 'SESSION' GROUP BY FUNCTION('HOUR', l.timestamp)")
    List<Object[]> getSessionsByHour();

    // 이벤트 분석
    @Query("SELECT l.additionalData, COUNT(l) FROM UserActivityLog l WHERE l.activityType = 'EVENT' GROUP BY l.additionalData")
    List<Object[]> getEventStats();

    List<UserActivityLog> findByUserId(Long userId);
}