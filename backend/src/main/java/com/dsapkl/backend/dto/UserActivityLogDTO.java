package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.UserActivityLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserActivityLogDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String activityType;
    private String url;
    private String pageId;
    private String additionalData;
    private LocalDateTime timestamp;

    public UserActivityLogDTO(UserActivityLog log, Member member) {
        this.id = log.getId();
        this.userId = log.getUserId();
        this.activityType = log.getActivityType();
        this.url = log.getUrl();
        this.pageId = log.getPageId();
        this.additionalData = log.getAdditionalData();
        this.timestamp = log.getTimestamp();

        // member가 null이 아닐 때만 사용자 정보 설정
        if (member != null) {
            this.userName = member.getName();
            this.userEmail = member.getEmail();
        }
    }
} 