package com.Hstep.Hstep.domain.chat.service;

import com.Hstep.Hstep.domain.chat.dto.ChatMessageDto;
import com.Hstep.Hstep.domain.chat.dto.ChatRoomDto;
import com.Hstep.Hstep.domain.chat.constant.ChatScenario;
import com.Hstep.Hstep.domain.chat.entity.ChatMessage;
import com.Hstep.Hstep.domain.chat.entity.ChatRole;
import com.Hstep.Hstep.domain.chat.entity.ChatRoom;
import com.Hstep.Hstep.domain.chat.exception.ChatResponseCode;
import com.Hstep.Hstep.domain.chat.repository.ChatMessageRepository;
import com.Hstep.Hstep.domain.chat.repository.ChatRoomRepository;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    public List<ChatRoomDto.Response> findMyRooms(String userId) {
        return chatRoomRepository.findByMember_UserIdOrderByUpdatedAtDesc(userId).stream()
                .map(ChatRoomDto.Response::from)
                .toList();
    }

    public List<ChatMessageDto.Response> findMessages(String userId, Long chatRoomId) {
        ChatRoom chatRoom = getOwnedRoom(userId, chatRoomId);
        return chatMessageRepository.findByChatRoom_ChatRoomIdOrderByCreatedAtAsc(chatRoom.getChatRoomId()).stream()
                .map(ChatMessageDto.Response::from)
                .toList();
    }

    @Transactional
    public void deleteRoom(String userId, Long chatRoomId) {
        ChatRoom chatRoom = getOwnedRoom(userId, chatRoomId);
        chatRoomRepository.delete(chatRoom);
    }

    @Transactional
    public ChatMessage appendMessage(String userId, Long chatRoomId, ChatRole role, String content) {
        ChatRoom chatRoom = getOwnedRoom(userId, chatRoomId);
        return chatMessageRepository.save(new ChatMessage(role, content, chatRoom));
    }

    @Transactional
    public ChatRoom getOrCreateRoadmapRoom(String userId, Long roadmapId) {
        return chatRoomRepository.findByMember_UserIdAndScenarioAndReferenceId(
                        userId, ChatScenario.AI_ROADMAP, roadmapId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(userId)
                            .orElseThrow(() -> new BaseException(ChatResponseCode.CHAT_ROOM_NOT_FOUND));
                    return chatRoomRepository.save(new ChatRoom(
                            "AI 개인 맞춤 로드맵", ChatScenario.AI_ROADMAP, member, roadmapId));
                });
    }

    @Transactional
    public ChatMessage appendRoadmapMessage(ChatRoom room, ChatRole role, String content, String proposalId) {
        return chatMessageRepository.save(new ChatMessage(role, content, room, proposalId));
    }

    public List<ChatMessage> findRoadmapMessages(String userId, Long roadmapId) {
        return chatRoomRepository.findByMember_UserIdAndScenarioAndReferenceId(
                        userId, ChatScenario.AI_ROADMAP, roadmapId)
                .map(room -> chatMessageRepository.findByChatRoom_ChatRoomIdOrderByCreatedAtAsc(room.getChatRoomId()))
                .orElse(List.of());
    }

    public ChatRoom findRoadmapRoom(String userId, Long roadmapId) {
        return chatRoomRepository.findByMember_UserIdAndScenarioAndReferenceId(
                        userId, ChatScenario.AI_ROADMAP, roadmapId)
                .orElse(null);
    }

    private ChatRoom getOwnedRoom(String userId, Long chatRoomId) {
        return chatRoomRepository.findByChatRoomIdAndMember_UserId(chatRoomId, userId)
                .orElseThrow(() -> new BaseException(ChatResponseCode.CHAT_ROOM_NOT_FOUND));
    }
}
