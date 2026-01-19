package com.hero.quest.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDetailResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDate deadline;
    private Integer reward;
    private Integer status;
    private String statusText;
    private LocalDateTime createdAt;
    private List<MemberInfo> members;

    @Data
    public static class MemberInfo {
        private Long id;
        private String memberName;
        private String memberEmail;
        private Integer earnedPoints;
        private Integer status;
        private String statusText;
        private LocalDateTime createdAt;
    }
}
