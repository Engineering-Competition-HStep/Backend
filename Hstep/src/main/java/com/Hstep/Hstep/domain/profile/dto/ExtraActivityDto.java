package com.Hstep.Hstep.domain.profile.dto;

import com.Hstep.Hstep.domain.profile.entity.ExtraActivity;
import java.time.LocalDateTime;

public class ExtraActivityDto {

    public record Request(String activityName, String fieldKeyword, String period, String description) {
    }

    public record Response(Long activityId, String activityName, String fieldKeyword, String period,
                           String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Response from(ExtraActivity activity) {
            return new Response(activity.getActivityId(), activity.getActivityName(), activity.getFieldKeyword(),
                    activity.getPeriod(), activity.getDescription(), activity.getCreatedAt(), activity.getUpdatedAt());
        }
    }
}