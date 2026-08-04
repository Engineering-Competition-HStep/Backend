package com.Hstep.Hstep.domain.chat.entity;

import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ai_chat")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_chat_id")
    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ChatRole role;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    public ChatMessage(ChatRole role, String content, ChatRoom chatRoom) {
        this.role = role;
        this.content = content;
        this.chatRoom = chatRoom;
    }
}