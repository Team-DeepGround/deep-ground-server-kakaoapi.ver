package com.samsamhajo.deepground.studyGroup.dto;

import com.samsamhajo.deepground.address.dto.AddressDto;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroup;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroupAddress;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroupMemberStatus;
import com.samsamhajo.deepground.studyGroup.entity.StudyGroupReply;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Getter
@Builder
public class StudyGroupDetailResponse {
  private Long id;
  private String title;
  private String explanation;
  private String writer;
  private int memberCount;
  private int groupLimit;
  private boolean isOffline;
  private LocalDate recruitStartDate;
  private LocalDate recruitEndDate;
  private LocalDate studyStartDate;
  private LocalDate studyEndDate;
  private int commentCount;
  private List<String> participants;
  private List<CommentWithRepliesResponse> comments;
  private StudyGroupMemberStatus memberStatus;
  private Set<TechTagDto> techStacks;
  private Set<AddressDto> addresses;

  public static StudyGroupDetailResponse from(StudyGroup group, Map<Long, List<StudyGroupReply>> replyMap, StudyGroupMemberStatus memberStatus) {
    return StudyGroupDetailResponse.builder()
            .id(group.getId())
            .title(group.getTitle())
            .explanation(group.getExplanation())
            .writer(group.getCreator().getNickname())
            .memberCount(group.getMembers().size())
            .groupLimit(group.getGroupMemberCount())
            .isOffline(group.getIsOffline())
            .recruitStartDate(group.getRecruitStartDate())
            .recruitEndDate(group.getRecruitEndDate())
            .studyStartDate(group.getStudyStartDate())
            .studyEndDate(group.getStudyEndDate())
            .commentCount(group.getComments().size())
            .memberStatus(memberStatus)
            .participants(
                group.getMembers().stream()
                    .map(m -> m.getMember().getNickname())
                    .collect(Collectors.toList())
            )
            .comments(
                group.getComments().stream()
                    .distinct()
                    .map(comment -> CommentWithRepliesResponse.from(comment, replyMap.getOrDefault(comment.getId(), List.of())))
                    .toList()
            )
            .techStacks(
                group.getStudyGroupTechTags().stream()
                    .map(studyGroupTechTag -> TechTagDto.from(studyGroupTechTag.getTechStack()))
                    .collect(Collectors.toSet())
            )
            .addresses(
                    group.getStudyGroupAddresses().stream()
                            .map(StudyGroupAddress::getAddress)
                            .map(AddressDto::from)
                            .collect(Collectors.toSet())
            )
            .build();
  }
}
