// domain/profile/dto/ProfileCompletenessDto.java
package com.Hstep.Hstep.domain.profile.dto;

public class ProfileCompletenessDto {

    public record Response(
            boolean trackCompleted,
            boolean gradeCompleted,
            boolean specCompleted,
            boolean completed
    ) {
        public static Response of(boolean trackCompleted, boolean gradeCompleted, boolean specCompleted) {
            return new Response(
                    trackCompleted, gradeCompleted, specCompleted,
                    trackCompleted && gradeCompleted && specCompleted
            );
        }
    }
}