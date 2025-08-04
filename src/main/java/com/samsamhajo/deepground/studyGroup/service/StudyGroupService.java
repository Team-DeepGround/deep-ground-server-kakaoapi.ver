package com.samsamhajo.deepground.studyGroup.service;

import com.samsamhajo.deepground.address.entity.Address;
import com.samsamhajo.deepground.address.exception.AddressErrorCode;
import com.samsamhajo.deepground.address.exception.AddressException;
import com.samsamhajo.deepground.address.repository.AddressRepository;
import com.samsamhajo.deepground.chat.service.ChatRoomService;
import com.samsamhajo.deepground.member.entity.MemberProfile;
import com.samsamhajo.deepground.studyGroup.dto.*;
import com.samsamhajo.deepground.studyGroup.entity.*;
import com.samsamhajo.deepground.studyGroup.exception.StudyGroupNotFoundException;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupTechTagRepository;
import com.samsamhajo.deepground.techStack.entity.TechStack;
import com.samsamhajo.deepground.techStack.repository.TechStackRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.samsamhajo.deepground.chat.entity.ChatRoom;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupMemberRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import java.time.LocalDate;
import java.util.*;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

  private final StudyGroupRepository studyGroupRepository;
  private final StudyGroupMemberRepository studyGroupMemberRepository;
  private final ChatRoomService chatRoomService;
  private final TechStackRepository techStackRepository;
  private final StudyGroupTechTagRepository studyGroupTechTagRepository;
  private final AddressRepository addressRepository;


  @Transactional
  public StudyGroupDetailResponse getStudyGroupDetail(Long studyGroupId, Long memberId) {
    StudyGroup group = studyGroupRepository.findWithAllButCommentsById(studyGroupId)
            .orElseThrow(() -> new StudyGroupNotFoundException(studyGroupId));

    List<StudyGroupComment> comments = studyGroupRepository.findCommentsWithMembersByStudyGroupId(studyGroupId);
    group.getComments().clear();
    group.getComments().addAll(comments);

    List<Long> commentIds = comments.stream().map(StudyGroupComment::getId).toList();
    List<StudyGroupReply> replies = studyGroupRepository.findRepliesByCommentIds(commentIds);

    Map<Long, List<StudyGroupReply>> replyMap = replies.stream()
            .collect(Collectors.groupingBy(r -> r.getComment().getId()));
    StudyGroupMemberStatus memberStatus = getMemberStatus(studyGroupId, memberId);

    return StudyGroupDetailResponse.from(group, replyMap, memberStatus);
  }

  public StudyGroupMemberStatus getMemberStatus(Long studyGroupId, Long memberId) {
    Optional<StudyGroupMember> memberOpt =
        studyGroupMemberRepository.findByStudyGroupIdAndMemberId(studyGroupId, memberId);

    return memberOpt.map(studyGroupMember -> studyGroupMember.getIsAllowed()
        ? StudyGroupMemberStatus.APPROVED
        : StudyGroupMemberStatus.PENDING).orElse(StudyGroupMemberStatus.NOT_APPLIED);

  }

  @Transactional(readOnly = true)
  public List<ParticipantSummaryDto> getParticipantSummaries(Long studyGroupId) {
    StudyGroup group = studyGroupRepository.findById(studyGroupId)
            .orElseThrow(() -> new StudyGroupNotFoundException(studyGroupId));

    return group.getMembers().stream()
            .map(m -> {
              Member member = m.getMember();
              MemberProfile profile = member.getMemberProfile();
              return ParticipantSummaryDto.builder()
                      .memberId(member.getId())
                      .profileId(profile.getProfileId())
                      .nickname(member.getNickname())
                      .profileImage(profile.getProfileImage())
                      .build();
            }).toList();
  }

  public Page<StudyGroupResponse> searchStudyGroups(StudyGroupSearchRequest request) {
    String keyword = request.getKeyword();
    GroupStatus status = request.getGroupStatus();
    Pageable pageable = request.toPageable();
    List<String> stackNames = request.getTechStackNames();

    Page<StudyGroup> pageResult = studyGroupRepository.searchWithFilters(status, keyword, stackNames, request.getOnOffline().name(), pageable);
    return pageResult.map(StudyGroupResponse::from);
  }

  private List<TechStack> getTechStacksByNames(List<String> names) {
    if (names == null || names.isEmpty()) return List.of();
    return techStackRepository.findByNames(names);
  }

  private void validatePeriod(LocalDate start, LocalDate end, String msg) {
    if (start.isAfter(end)) {
      throw new IllegalArgumentException(msg);
    }
  }

  @Transactional
  public StudyGroupDetailResponse updateStudyGroup(Long studyGroupId, StudyGroupUpdateRequest request, Member updater) {
    StudyGroup group = studyGroupRepository.findWithAllButCommentsById(studyGroupId)
        .orElseThrow(() -> new StudyGroupNotFoundException(studyGroupId));

    if (!group.getCreator().getId().equals(updater.getId())) {
      throw new IllegalArgumentException("스터디 생성자만 수정할 수 있습니다.");
    }

    validatePeriod(request.getStudyStartDate(), request.getStudyEndDate(), "스터디 시작일은 종료일보다 이전이어야 합니다.");
    validatePeriod(request.getRecruitStartDate(), request.getRecruitEndDate(), "모집 시작일은 종료일보다 이전이어야 합니다.");

    if (request.getGroupMemberCount() <= 0) {
      throw new IllegalArgumentException("정원은 1명 이상이어야 합니다.");
    }

    List<TechStack> techStacks = getTechStacksByNames(request.getTechStackNames());
    group.update(request, techStacks);

    if (request.getIsOffline() && request.getAddressIds() != null) {
      List<Address> addresses = addressRepository.findAllById(request.getAddressIds());

      if (addresses.size() != request.getAddressIds().size()) {
        throw new AddressException(AddressErrorCode.INVALID_ADDRESS_INCLUDED);
      }

      group.getStudyGroupAddresses().clear();

      for (Address address : addresses) {
        StudyGroupAddress sga = StudyGroupAddress.of(null, address);
        sga.assignStudyGroup(group);
      }
    }

    List<StudyGroupComment> comments = studyGroupRepository.findCommentsWithMembersByStudyGroupId(studyGroupId);
    group.getComments().clear();
    group.getComments().addAll(comments);

    List<Long> commentIds = comments.stream().map(StudyGroupComment::getId).toList();
    List<StudyGroupReply> replies = studyGroupRepository.findRepliesByCommentIds(commentIds);
    Map<Long, List<StudyGroupReply>> replyMap = replies.stream().collect(Collectors.groupingBy(r -> r.getComment().getId()));

    StudyGroupMemberStatus memberStatus = getMemberStatus(group.getId(), updater.getId());

    return StudyGroupDetailResponse.from(group, replyMap, memberStatus);
  }

  @Transactional
  public StudyGroupCreateResponse createStudyGroup(StudyGroupCreateRequest request, Member creator) {
    validateRequest(request);
    ChatRoom chatRoom = chatRoomService.createStudyGroupChatRoom(creator);

    StudyGroup studyGroup = StudyGroup.of(
            chatRoom,
            request.getTitle(),
            request.getExplanation(),
            request.getStudyStartDate(),
            request.getStudyEndDate(),
            request.getRecruitStartDate(),
            request.getRecruitEndDate(),
            request.getGroupMemberCount(),
            creator,
            request.getIsOffline(),
            new ArrayList<>()
    );
    studyGroupRepository.save(studyGroup);

    if (request.getIsOffline() && request.getAddressIds() != null) {
      List<Address> addresses = addressRepository.findAllById(request.getAddressIds());

      if (addresses.size() != request.getAddressIds().size()) {
        throw new AddressException(AddressErrorCode.INVALID_ADDRESS_INCLUDED);
      }

      for (Address address : addresses) {
        StudyGroupAddress sga = StudyGroupAddress.of(null, address);
        sga.assignStudyGroup(studyGroup);
      }
    }

    List<TechStack> techStacks = getTechStacksByNames(request.getTechStackNames());
    for (TechStack techStack : techStacks) {
      StudyGroupTechTag link = StudyGroupTechTag.of(studyGroup, techStack);
      studyGroupTechTagRepository.save(link);
    }
    StudyGroupMember groupMember = StudyGroupMember.of(creator, studyGroup, true);
    studyGroupMemberRepository.save(groupMember);
    return StudyGroupCreateResponse.from(studyGroup);
  }

  public List<StudyGroupParticipationResponse> getStudyGroupsByMember(Long memberId) {
    List<StudyGroupMember> studyGroupMembers =
        studyGroupMemberRepository.findAllByMemberIdAndIsAllowedTrueAndNotCreator(
            memberId);

    return studyGroupMembers.stream()
        .map(member -> StudyGroupParticipationResponse.from(
            member.getStudyGroup(),
            member.getCreatedAt()
        )).toList();
  }
      
  public List<StudyGroupMyListResponse> findMyStudyGroups(Long memberId) {
    List<StudyGroup> groups = studyGroupRepository.findAllByCreator_IdOrderByCreatedAtDesc(memberId);

    return groups.stream()
        .map(StudyGroupMyListResponse::from)
        .toList();
  }

  @Transactional
  public void softDeleteStudyGroup(Long studyGroupId, Member requester) {
    var studyGroup = studyGroupRepository.findById(studyGroupId)
        .orElseThrow(() -> new StudyGroupNotFoundException(studyGroupId));

    if (!studyGroup.getCreator().getId().equals(requester.getId())) {
      throw new IllegalArgumentException("스터디 생성자만 삭제할 수 있습니다.");
    }

    // 채팅방 삭제
    chatRoomService.deleteChatRoom(studyGroup.getChatRoom().getId());

    studyGroup.softDelete();
  }


  private void validateRequest(StudyGroupCreateRequest request) {
    LocalDate now = LocalDate.now();

    if (request.getRecruitEndDate().isBefore(now)) {
      throw new IllegalArgumentException("모집 마감일은 현재 시점보다 미래여야 합니다.");
    }

    if (request.getStudyStartDate().isAfter(request.getStudyEndDate())) {
      throw new IllegalArgumentException("스터디 시작일은 종료일보다 이전이어야 합니다.");
    }

    if (request.getGroupMemberCount() <= 0) {
      throw new IllegalArgumentException("정원은 1명 이상이어야 합니다.");
    }

    // 필요한 경우 추가 유효성 체크 가능
  }
}
