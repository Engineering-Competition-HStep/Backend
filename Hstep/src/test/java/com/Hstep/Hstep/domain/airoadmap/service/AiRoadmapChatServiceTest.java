package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.entity.*;
import com.Hstep.Hstep.domain.airoadmap.repository.AiRoadmapChangeProposalRepository;
import com.Hstep.Hstep.domain.chat.constant.ChatScenario;
import com.Hstep.Hstep.domain.chat.entity.ChatRoom;
import com.Hstep.Hstep.domain.chat.entity.ChatMessage;
import com.Hstep.Hstep.domain.chat.entity.ChatRole;
import com.Hstep.Hstep.domain.chat.service.ChatService;
import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import com.Hstep.Hstep.domain.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiRoadmapChatServiceTest {

    private final AiRoadmapService roadmapService = mock(AiRoadmapService.class);
    private final AiRoadmapProfileAnalyzer analyzer = mock(AiRoadmapProfileAnalyzer.class);
    private final AiRoadmapIntentClassifier classifier = mock(AiRoadmapIntentClassifier.class);
    private final AiRoadmapChangeProposalRepository proposalRepository = mock(AiRoadmapChangeProposalRepository.class);
    private final ChatService chatService = mock(ChatService.class);
    private AiRoadmapChatService service;
    private Member member;
    private AiRoadmap roadmap;
    private AiRoadmapItem item;

    @BeforeEach
    void setUp() {
        service = new AiRoadmapChatService(roadmapService, analyzer, classifier, proposalRepository, chatService);
        member = Member.create("user", "user@test.com", "encoded", "사용자", 3);
        roadmap = AiRoadmap.create(member, Job.create("백엔드개발자", JobCategory.SOFTWARE, "테스트"));
        item = AiRoadmapItem.createCustom(roadmap, "Docker 배포", "설명", RoadmapLane.LEARNING,
                RoadmapItemType.DEVELOPMENT_TOOL, RoadmapStage.GRADE_3, 10,
                AiRoadmapStandardItem.Priority.HIGH);
        org.springframework.test.util.ReflectionTestUtils.setField(item, "aiRoadmapItemId", 1L);
        when(roadmapService.findRoadmap("user")).thenReturn(roadmap);
        when(roadmapService.findOwnedItem(roadmap, 1L)).thenReturn(item);
        when(roadmapService.getAllItems(roadmap)).thenReturn(List.of(item));
        when(analyzer.getMemberWithTracks("user")).thenReturn(member);
        when(chatService.getOrCreateRoadmapRoom("user", null))
                .thenReturn(new ChatRoom("AI 로드맵", ChatScenario.AI_ROADMAP, member, null));
        when(proposalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void chat은_완료_제안만_저장하고_apply_이후에만_항목을_변경한다() {
        when(classifier.command("완료해줘")).thenReturn(new AiRoadmapCommand(
                AiRoadmapChangeProposal.ActionType.COMPLETE_ITEM, 1L, null, null, "완료"));
        AiRoadmapDto.ChatRequest request = request("완료해줘", 1L);

        AiRoadmapDto.ChatResponse response = service.chat("user", request);

        assertThat(item.getStatus()).isEqualTo(AiRoadmapItem.Status.PENDING);
        assertThat(response.proposal()).isNotNull();
        assertThat(response.proposal().status()).isEqualTo(AiRoadmapChangeProposal.Status.PENDING);

        String proposalId = response.proposal().proposalId();
        AiRoadmapChangeProposal proposal = response.proposal() == null ? null
                : captureSavedProposal();
        when(proposalRepository.findByProposalIdAndMember_UserId(proposalId, "user"))
                .thenReturn(Optional.of(proposal));
        when(roadmapService.toRoadmapResponse(member, roadmap, null, null))
                .thenReturn(mock(AiRoadmapDto.RoadmapResponse.class));

        service.apply("user", proposalId);

        assertThat(item.getStatus()).isEqualTo(AiRoadmapItem.Status.COMPLETED);
        assertThat(proposal.getStatus()).isEqualTo(AiRoadmapChangeProposal.Status.APPLIED);
    }

    @Test
    void cancel은_항목을_변경하지_않고_제안만_취소한다() {
        when(classifier.command("제외해줘")).thenReturn(new AiRoadmapCommand(
                AiRoadmapChangeProposal.ActionType.REMOVE_ITEM, 1L, null, null, "제외"));
        AiRoadmapDto.ChatResponse response = service.chat("user", request("제외해줘", 1L));
        AiRoadmapChangeProposal proposal = captureSavedProposal();
        when(proposalRepository.findByProposalIdAndMember_UserId(response.proposal().proposalId(), "user"))
                .thenReturn(Optional.of(proposal));

        service.cancel("user", response.proposal().proposalId());

        assertThat(item.getStatus()).isEqualTo(AiRoadmapItem.Status.PENDING);
        assertThat(proposal.getStatus()).isEqualTo(AiRoadmapChangeProposal.Status.CANCELED);
    }

    @Test
    void history는_USER_ASSISTANT_순서와_연결된_proposal_상태를_복원한다() {
        ChatRoom room = new ChatRoom("AI 로드맵", ChatScenario.AI_ROADMAP, member, null);
        org.springframework.test.util.ReflectionTestUtils.setField(room, "chatRoomId", 10L);
        AiRoadmapChangeProposal proposal = AiRoadmapChangeProposal.create(member, roadmap,
                AiRoadmapChangeProposal.ActionType.MOVE_ITEM, 1L, null, null, null,
                "이동", java.util.Map.of("targetStage", "GRADE_3"),
                java.util.Map.of("targetStage", "GRADE_4"));
        ChatMessage userMessage = new ChatMessage(ChatRole.USER, "4학년으로 옮겨줘", room);
        ChatMessage assistantMessage = new ChatMessage(ChatRole.ASSISTANT, "4학년으로 이동합니다.",
                room, proposal.getProposalId());
        when(chatService.findRoadmapRoom("user", null)).thenReturn(room);
        when(chatService.findRoadmapMessages("user", null))
                .thenReturn(List.of(userMessage, assistantMessage));
        when(proposalRepository.findById(proposal.getProposalId())).thenReturn(Optional.of(proposal));

        AiRoadmapDto.ChatHistoryResponse history = service.getHistory("user");

        assertThat(history.chatRoomId()).isEqualTo(10L);
        assertThat(history.messages()).extracting(AiRoadmapDto.ChatHistoryMessageResponse::role)
                .containsExactly(ChatRole.USER, ChatRole.ASSISTANT);
        assertThat(history.messages().get(1).proposalStatus())
                .isEqualTo(AiRoadmapChangeProposal.Status.PENDING);
        assertThat(history.messages().get(1).actionable()).isTrue();
    }

    private AiRoadmapChangeProposal captureSavedProposal() {
        var captor = org.mockito.ArgumentCaptor.forClass(AiRoadmapChangeProposal.class);
        verify(proposalRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues().getLast();
    }

    private AiRoadmapDto.ChatRequest request(String message, Long itemId) {
        return new AiRoadmapDto.ChatRequest(message, null, null, itemId, null,
                null, null, null, null);
    }
}
