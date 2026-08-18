package com.Hstep.Hstep.domain.chat.repository;

import com.Hstep.Hstep.domain.chat.entity.ChatRoom;
import com.Hstep.Hstep.domain.chat.constant.ChatScenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByMember_UserIdOrderByUpdatedAtDesc(String userId);

    Optional<ChatRoom> findByChatRoomIdAndMember_UserId(Long chatRoomId, String userId);

    Optional<ChatRoom> findByMember_UserIdAndScenarioAndReferenceId(
            String userId, ChatScenario scenario, Long referenceId);
}
