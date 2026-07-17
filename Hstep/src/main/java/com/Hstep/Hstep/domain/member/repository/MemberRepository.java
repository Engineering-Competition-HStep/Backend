package com.Hstep.Hstep.domain.member.repository;

import com.Hstep.Hstep.domain.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "memberTracks")
    @Query("select member from Member member where member.userId = :userId")
    Optional<Member> findWithTracksByUserId(String userId);
}
