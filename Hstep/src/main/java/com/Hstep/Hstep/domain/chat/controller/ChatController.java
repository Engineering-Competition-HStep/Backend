package com.Hstep.Hstep.domain.chat.controller;

import com.Hstep.Hstep.domain.chat.dto.ChatMessageDto;
import com.Hstep.Hstep.domain.chat.dto.ChatRoomDto;
import com.Hstep.Hstep.domain.chat.dto.ChatStartDto;
import com.Hstep.Hstep.domain.chat.service.ChatConversationService;
import com.Hstep.Hstep.domain.chat.service.ChatService;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatController {

    private final ChatService chatService;
    private final ChatConversationService chatConversationService;

    @GetMapping
    public ResponseEntity<List<ChatRoomDto.Response>> findMyRooms(@AuthenticationPrincipal MemberPrincipal principal) {
        return ResponseEntity.ok(chatService.findMyRooms(principal.getUserId()));
    }

    @PostMapping("/start")
    public ResponseEntity<ChatStartDto.Response> start(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody ChatStartDto.Request request
    ) {
        return ResponseEntity.ok(chatConversationService.start(principal.getUserId(), request.scenario()));
    }

    @PostMapping("/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageDto.Response> sendMessage(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long chatRoomId,
            @RequestBody ChatMessageDto.Request request
    ) {
        return ResponseEntity.ok(chatConversationService.sendMessage(principal.getUserId(), chatRoomId, request.content()));
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageDto.Response>> findMessages(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long chatRoomId
    ) {
        return ResponseEntity.ok(chatService.findMessages(principal.getUserId(), chatRoomId));
    }

    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<Void> deleteRoom(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long chatRoomId
    ) {
        chatService.deleteRoom(principal.getUserId(), chatRoomId);
        return ResponseEntity.noContent().build();
    }
}