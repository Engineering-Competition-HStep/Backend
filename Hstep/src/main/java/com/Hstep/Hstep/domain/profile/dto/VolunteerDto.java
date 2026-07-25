package com.Hstep.Hstep.domain.profile.dto;

import com.Hstep.Hstep.domain.profile.entity.Volunteer;
import java.time.LocalDateTime;

public class VolunteerDto {

    public record Request(String volunteerName, Integer volunteerHours, String description) {
    }

    public record Response(Long volunteerId, String volunteerName, Integer volunteerHours,
                           String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Response from(Volunteer volunteer) {
            return new Response(volunteer.getVolunteerId(), volunteer.getVolunteerName(),
                    volunteer.getVolunteerHours(), volunteer.getDescription(),
                    volunteer.getCreatedAt(), volunteer.getUpdatedAt());
        }
    }
}