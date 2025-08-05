package com.samsamhajo.deepground.member.repository;

import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
    // 닉네임 존재 여부 확인
    boolean existsByNickname(String nickname);

    Optional<Member> findByEmail(String email);

    String email(String email);

    Long countByCreatedAtAfter(LocalDateTime time);
}



