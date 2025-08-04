package com.samsamhajo.deepground.studyGroup.service;


import com.samsamhajo.deepground.IntegrationTestSupport;
import com.samsamhajo.deepground.address.entity.Address;
import com.samsamhajo.deepground.address.repository.AddressRepository;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.studyGroup.dto.StudyGroupInviteRequest;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroup;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroupAddress;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupInviteTokenRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import com.samsamhajo.deepground.chat.entity.ChatRoom;
import com.samsamhajo.deepground.chat.entity.ChatRoomType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class StudyGroupInviteServiceTest extends IntegrationTestSupport {

  @Autowired private StudyGroupInviteService inviteService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private StudyGroupRepository studyGroupRepository;
  @Autowired private StudyGroupInviteTokenRepository inviteTokenRepository;
  @Autowired private AddressRepository addressRepository;

  private Member owner;
  private Member outsider;
  private StudyGroup group;

  @BeforeEach
  void setUp() {
    owner = Member.createLocalMember("owner@test.com", "pw", "운영자");
    outsider = Member.createLocalMember("user@test.com", "pw", "외부인");
    memberRepository.save(owner);
    memberRepository.save(outsider);

    Address address = Address.of("서울", "강남구", "강남역");  // 필요 시 생성자에 맞게 수정
    addressRepository.save(address);

    StudyGroupAddress groupAddress = StudyGroupAddress.of(null, address);

    group = StudyGroup.of(
        ChatRoom.of(ChatRoomType.STUDY_GROUP), "스터디", "소개",
        LocalDate.now(), LocalDate.now().plusDays(10),
        LocalDate.now(), LocalDate.now().plusDays(3),
        5, owner, true, List.of(groupAddress)
    );
    studyGroupRepository.save(group);
  }

  @Test
  @DisplayName("스터디장이 아닌 사용자가 초대하면 예외가 발생한다")
  void inviteByUnauthorizedUser() {
    StudyGroupInviteRequest request = StudyGroupInviteRequest.builder()
        .studyGroupId(group.getId())
        .inviteeEmail("invitee@test.com")
        .build();

    assertThatThrownBy(() -> inviteService.inviteByEmail(outsider, request))
        .isInstanceOf(SecurityException.class);
  }

  @Test
  @DisplayName("중복된 이메일 초대 시 예외가 발생한다")
  void inviteDuplicateEmail() {
    StudyGroupInviteRequest request = StudyGroupInviteRequest.builder()
        .studyGroupId(group.getId())
        .inviteeEmail("already@test.com")
        .build();

    inviteService.inviteByEmail(owner, request); // 최초 초대

    assertThatThrownBy(() -> inviteService.inviteByEmail(owner, request))
        .isInstanceOf(IllegalStateException.class);
  }
}
