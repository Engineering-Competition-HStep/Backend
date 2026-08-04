package com.Hstep.Hstep.domain.chat.dto;

import com.Hstep.Hstep.domain.chat.entity.ChatMessage;
import com.Hstep.Hstep.domain.chat.entity.ChatRole;

import java.time.LocalDateTime;

public class ChatMessageDto {

    public record Response(
            Long chatId,
            ChatRole role,
            String content,
            LocalDateTime createdAt
    ) {
        public static Response from(ChatMessage chatMessage) {
            return new Response(
                    chatMessage.getChatId(),
                    chatMessage.getRole(),
                    chatMessage.getContent(),
                    chatMessage.getCreatedAt()
            );
        }
    }
}