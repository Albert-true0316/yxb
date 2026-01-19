package com.hero.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hero.quest.dto.LeaderboardEntry;
import com.hero.quest.entity.TaskMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface TaskMemberMapper extends BaseMapper<TaskMember> {

    @Select("""
        SELECT
            tm.member_email as memberEmail,
            MAX(tm.member_name) as memberName,
            COALESCE(SUM(tm.earned_points), 0) as totalPoints,
            COUNT(DISTINCT tm.task_id) as completedTasks
        FROM task_member tm
        INNER JOIN task t ON tm.task_id = t.id
        WHERE t.status = 3
          AND tm.status = 1
          AND tm.earned_points > 0
        GROUP BY tm.member_email
        ORDER BY totalPoints DESC
        LIMIT #{limit}
    """)
    List<LeaderboardEntry> getLeaderboard(int limit);
}
