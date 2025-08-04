package com.samsamhajo.deepground.studyGroup.service;

import com.samsamhajo.deepground.IntegrationTestSupport;
import com.samsamhajo.deepground.address.entity.Address;
import com.samsamhajo.deepground.address.repository.AddressRepository;
import com.samsamhajo.deepground.chat.entity.ChatRoomType;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.entity.Provider;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.studyGroup.dto.StudyGroupCreateRequest;
import com.samsamhajo.deepground.studyGroup.dto.StudyGroupCreateResponse;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroup;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroupMember;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupMemberRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import com.samsamhajo.deepground.techStack.entity.TechStack;
import com.samsamhajo.deepground.techStack.repository.TechStackRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
class StudyGroupServiceIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private StudyGroupService studyGroupService;

  @Autowired
  private StudyGroupRepository studyGroupRepository;

  @Autowired
  private StudyGroupMemberRepository studyGroupMemberRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private AddressRepository addressRepository;

  @Autowired
  private TechStackRepository techStackRepository;

  private Member creator;

  @BeforeEach
  void setUp() {
    creator = Member.createLocalMember("test@example.com", "securePassword", "테스트유저");
    memberRepository.save(creator);
  }

  @Test
  void createStudyGroup_successfully() {
    // given
    Address address = Address.of("서울시", "마포구", "신촌동");
    addressRepository.save(address);

    StudyGroupCreateRequest request = StudyGroupCreateRequest.builder()
        .title("모각코 스터디")
        .explanation("매일 오전 9시 모여서 코딩하는 스터디입니다.")
        .studyStartDate(LocalDate.now().plusDays(7))
        .studyEndDate(LocalDate.now().plusDays(30))
        .recruitStartDate(LocalDate.now())
        .recruitEndDate(LocalDate.now().plusDays(5))
        .groupMemberCount(5)
        .isOffline(true)
            .addressIds(List.of(address.getId()))
        .build();

    // when
    StudyGroupCreateResponse response = studyGroupService.createStudyGroup(request, creator);

    // then
    Optional<StudyGroup> savedGroup = studyGroupRepository.findById(response.getId());
    assertThat(savedGroup).isPresent();

    StudyGroup group = savedGroup.get();
    assertThat(group.getTitle()).isEqualTo(request.getTitle());
    assertThat(group.getCreator().getId()).isEqualTo(creator.getId());

    Optional<StudyGroupMember> membership = studyGroupMemberRepository.findAll().stream()
        .filter(m -> m.getMember().getId().equals(creator.getId()) &&
            m.getStudyGroup().getId().equals(group.getId()))
        .findFirst();

    assertThat(membership).isPresent();
    assertThat(membership.get().getIsAllowed()).isTrue();

    assertThat(group.getChatRoom()).isNotNull();
//    assertThat(group.getChatRoom().getChatRoomType()).isEqualTo(ChatRoomType.STUDY_GROUP);
  }
}
