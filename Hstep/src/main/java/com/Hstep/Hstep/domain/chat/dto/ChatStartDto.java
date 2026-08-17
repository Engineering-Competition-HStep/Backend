package com.Hstep.Hstep.domain.chat.dto;

import com.Hstep.Hstep.domain.chat.constant.ChatScenario;
import com.Hstep.Hstep.domain.profile.dto.ProfileCompletenessDto;

public class ChatStartDto {

    public record Request(ChatScenario scenario) {}

    public record Response(
            boolean started,
            ProfileCompletenessDto.Response completeness,
            Long chatRoomId,
            ChatMessageDto.Response firstMessage
    ) {
        public static Response blocked(ProfileCompletenessDto.Response completeness) {
            return new Response(false, completeness, null, null);
        }

        public static Response started(ProfileCompletenessDto.Response completeness, Long chatRoomId, ChatMessageDto.Response firstMessage) {
            return new Response(true, completeness, chatRoomId, firstMessage);
        }
    }
}