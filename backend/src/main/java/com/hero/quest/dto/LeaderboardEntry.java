package com.hero.quest.dto;

import lombok.Data;

@Data
public class LeaderboardEntry {

    private String memberEmail;
    private String memberName;
    private Integer totalPoints;
    private Integer completedTasks;
    private Integer rank;
}
