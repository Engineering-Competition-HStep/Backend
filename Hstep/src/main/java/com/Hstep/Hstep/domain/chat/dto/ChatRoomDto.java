package com.Hstep.Hstep.domain.chat.dto;

import com.Hstep.Hstep.domain.chat.entity.ChatRoom;

import java.time.LocalDateTime;

public class ChatRoomDto {

    public record CreateRequest(
            String title
    ) {}

    public record Response(
            Long chatRoomId,
            String title,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static Response from(ChatRoom chatRoom) {
            return new Response(
                    chatRoom.getChatRoomId(),
                    chatRoom.getTitle(),
                    chatRoom.getCreatedAt(),
                    chatRoom.getUpdatedAt()
            );
        }
    }
}