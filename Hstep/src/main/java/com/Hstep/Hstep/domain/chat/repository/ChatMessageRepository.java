package com.Hstep.Hstep.domain.chat.repository;

import com.Hstep.Hstep.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoom_ChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}