package com.hero.quest.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskSummaryResponse {
    private Long id;
    private String title;
    private LocalDate deadline;
    private Integer reward;
    private Integer status;
    private LocalDateTime createdAt;
    private List<String> memberNames;
}
