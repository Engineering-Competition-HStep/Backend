package com.Hstep.Hstep.domain.chat.service;

import com.Hstep.Hstep.domain.chat.constant.ChatScenario;
import com.Hstep.Hstep.domain.chat.dto.ChatMessageDto;
import com.Hstep.Hstep.domain.chat.dto.ChatStartDto;
import com.Hstep.Hstep.domain.chat.entity.ChatMessage;
import com.Hstep.Hstep.domain.chat.entity.ChatRole;
import com.Hstep.Hstep.domain.chat.entity.ChatRoom;
import com.Hstep.Hstep.domain.chat.exception.ChatResponseCode;
import com.Hstep.Hstep.domain.chat.prompt.ChatContextBuilder;
import com.Hstep.Hstep.domain.chat.prompt.ChatSystemPrompts;
import com.Hstep.Hstep.domain.chat.repository.ChatMessageRepository;
import com.Hstep.Hstep.domain.chat.repository.ChatRoomRepository;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.dto.ProfileCompletenessDto;
import com.Hstep.Hstep.domain.profile.repository.*;
import com.Hstep.Hstep.domain.profile.service.ProfileCompletenessService;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatConversationService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ProfileCompletenessService profileCompletenessService;

    private final CertificateRepository certificateRepository;
    private final AwardRepository awardRepository;
    private final VolunteerRepository volunteerRepository;
    private final ExtraActivityRepository extraActivityRepository;
    private final UserGradeGpaRepository userGradeGpaRepository;

    // TODO: Track / BaseRoadmapItem 레포지토리 - 아래 buildContextBlock() 완성에 필요

    private final ChatClient chatClient;

    @Transactional
    public ChatStartDto.Response start(String userId, ChatScenario scenario) {
        ProfileCompletenessDto.Response completeness = profileCompletenessService.check(userId);
        if (!completeness.completed()) {
            return ChatStartDto.Response.blocked(completeness);
        }

        Member member = memberRepository.getReferenceById(userId);
        ChatRoom chatRoom = chatRoomRepository.save(new ChatRoom(scenarioTitle(scenario), scenario, member));

        String contextBlock = buildContextBlock(userId);
        chatMessageRepository.save(new ChatMessage(ChatRole.USER, contextBlock, chatRoom));

        List<Message> messages = List.of(
                new SystemMessage(systemPromptFor(scenario)),
                new UserMessage(contextBlock)
        );
        String replyText = chatClient.prompt().messages(messages).call().content();

        ChatMessage assistantMessage = chatMessageRepository.save(
                new ChatMessage(ChatRole.ASSISTANT, replyText, chatRoom));

        return ChatStartDto.Response.started(completeness, chatRoom.getChatRoomId(), ChatMessageDto.Response.from(assistantMessage));
    }

    @Transactional
    public ChatMessageDto.Response sendMessage(String userId, Long chatRoomId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findByChatRoomIdAndMember_UserId(chatRoomId, userId)
                .orElseThrow(() -> new BaseException(ChatResponseCode.CHAT_ROOM_NOT_FOUND));

        chatMessageRepository.save(new ChatMessage(ChatRole.USER, content, chatRoom));

        List<ChatMessage> history = chatMessageRepository.findByChatRoom_ChatRoomIdOrderByCreatedAtAsc(chatRoomId);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPromptFor(chatRoom.getScenario())));
        for (ChatMessage m : history) {
            messages.add(m.getRole() == ChatRole.USER
                    ? new UserMessage(m.getContent())
                    : new AssistantMessage(m.getContent()));
        }

        String replyText = chatClient.prompt().messages(messages).call().content();

        ChatMessage assistantMessage = chatMessageRepository.save(
                new ChatMessage(ChatRole.ASSISTANT, replyText, chatRoom));

        return ChatMessageDto.Response.from(assistantMessage);
    }

    private String systemPromptFor(ChatScenario scenario) {
        return switch (scenario) {
            case TRACK_CAREER_ANALYSIS -> ChatSystemPrompts.TRACK_CAREER_ANALYSIS;
            case RECOMMENDED_JOB -> ChatSystemPrompts.RECOMMENDED_JOB;
        };
    }

    private String scenarioTitle(ChatScenario scenario) {
        return switch (scenario) {
            case TRACK_CAREER_ANALYSIS -> "내 트랙 취업 분석";
            case RECOMMENDED_JOB -> "추천 직무";
        };
    }

    private String buildContextBlock(String userId) {
        String certificates = formatOrNone(certificateRepository.findByMember_UserId(userId).stream()
                .map(c -> c.getCertificateName() + "(" + c.getIssuedYear() + ")").toList());
        String awards = formatOrNone(awardRepository.findByMember_UserId(userId).stream()
                .map(a -> a.getCompetitionName() + " - " + a.getAwardName()).toList());
        String volunteers = formatOrNone(volunteerRepository.findByMember_UserId(userId).stream()
                .map(v -> v.getVolunteerName() + "(" + v.getOrganizationName() + ", " + v.getVolunteerHours() + "시간)").toList());
        String activities = formatOrNone(extraActivityRepository.findByMember_UserId(userId).stream()
                .map(e -> e.getActivityName()).toList());

        // TODO: 트랙명(1/2트랙), 평균학점, 트랙 기준 추천 로드맵은
        // Track/MemberTrack/BaseRoadmapItem 조회 로직 완성 후 채워야 함
        return ChatContextBuilder.build(
                "TODO", "TODO", 0, "TODO",
                certificates, awards, volunteers, activities, "TODO"
        );
    }

    private String formatOrNone(List<String> items) {
        return items.isEmpty() ? "없음" : String.join(", ", items);
    }
}