package com.samsamhajo.deepground.studyGroup.service;

import com.samsamhajo.deepground.IntegrationTestSupport;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.studyGroup.dto.StudyGroupCommentRequest;
import com.samsamhajo.deepground.studyGroup.dto.StudyGroupCommentResponse;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroup;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupCommentRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;

@Transactional
class StudyGroupCommentServiceTest extends IntegrationTestSupport {

  @Autowired
  private StudyGroupCommentService commentService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private StudyGroupRepository studyGroupRepository;

  @Autowired
  private StudyGroupCommentRepository commentRepository;

  @PersistenceContext
  private EntityManager em;

  private Long memberId;
  private Long studyGroupId;

  @BeforeEach
  void setUp() {
    Member member = Member.createLocalMember("user@test.com", "1234", "댓글작성자");
    memberRepository.save(member);
    this.memberId = member.getId();

    StudyGroup group = StudyGroup.of(
        null, "댓글 테스트 스터디", "설명",
        LocalDate.now().plusDays(1),
        LocalDate.now().plusDays(10),
        LocalDate.now(),
        LocalDate.now().plusDays(5),
        10,
        member,
        true
    );
    studyGroupRepository.save(group);
    this.studyGroupId = group.getId();

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("유효한 요청이면 댓글이 저장되고 응답된다")
  void writeComment_success() {
    // given
    StudyGroupCommentRequest request = StudyGroupCommentRequest.builder()
        .studyGroupId(studyGroupId)
        .content("이 스터디 괜찮네요!")
        .build();

    // when
    StudyGroupCommentResponse response = commentService.writeComment(request, memberId);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getCommentId()).isNotNull();
    assertThat(response.getContent()).isEqualTo("이 스터디 괜찮네요!");
    assertThat(response.getWriterNickname()).isEqualTo("댓글작성자");
  }

  @Test
  @DisplayName("존재하지 않는 사용자로 댓글 작성 시 예외 발생")
  void writeComment_invalidMember() {
    // given
    Long invalidMemberId = -1L;
    StudyGroupCommentRequest request = StudyGroupCommentRequest.builder()
        .studyGroupId(studyGroupId)
        .content("내용")
        .build();

    // expect
    assertThatThrownBy(() -> commentService.writeComment(request, invalidMemberId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("존재하지 않는 사용자입니다.");
  }

  @Test
  @DisplayName("존재하지 않는 스터디로 댓글 작성 시 예외 발생")
  void writeComment_invalidStudyGroup() {
    // given
    Long invalidGroupId = -999L;
    StudyGroupCommentRequest request = StudyGroupCommentRequest.builder()
        .studyGroupId(invalidGroupId)
        .content("내용")
        .build();

    // expect
    assertThatThrownBy(() -> commentService.writeComment(request, memberId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("존재하지 않는 스터디 그룹입니다.");
  }
}
