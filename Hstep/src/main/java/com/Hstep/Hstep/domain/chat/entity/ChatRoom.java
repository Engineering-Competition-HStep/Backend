package com.Hstep.Hstep.domain.chat.entity;

import com.Hstep.Hstep.domain.chat.constant.ChatScenario;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ai_chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_chat_room_id")
    private Long chatRoomId;

    @Column(name = "title", length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario", nullable = false, length = 30)
    private ChatScenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    public ChatRoom(String title, ChatScenario scenario, Member member) {
        this.title = title;
        this.scenario = scenario;
        this.member = member;
    }

    public void updateTitle(String title) {
        this.title = title;
    }
}