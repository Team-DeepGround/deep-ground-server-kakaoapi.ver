package com.samsamhajo.deepground.studyGroup.dto;

import com.samsamhajo.deepground.address.dto.AddressDto;
import com.samsamhajo.deepground.address.entity.Address;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.studyGroup.entity.*;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Getter
@Builder
public class StudyGroupResponse {
  private Long id;
  private String title;
  private String description;
  private String period;
  private String recruitmentPeriod;
  private GroupStatus groupStatus;
  private Set<TechTagDto> tags;
  private Integer maxMembers;
  private Integer currentMembers;
  private OrganizerDto organizer;
  private Boolean isOnline;
  private Set<AddressDto> addresses;

  @Getter
  @Builder
  public static class OrganizerDto {
    private String name;
    private String avatar;
  }

  public static StudyGroupResponse from(StudyGroup group) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    Member creator = group.getCreator();

    Set<TechTagDto> techTags = group.getStudyGroupTechTags().stream()
            .map(StudyGroupTechTag::getTechStack)
            .map(techStack -> new TechTagDto(techStack.getId(), techStack.getName()))
            .collect(Collectors.toSet());

    Set<AddressDto> addresses = new LinkedHashSet<>(
            group.getStudyGroupAddresses().stream()
                    .map(StudyGroupAddress::getAddress)
                    .sorted(Comparator.comparing(Address::getId))
                    .map(AddressDto::from)
                    .toList()
    );

    return StudyGroupResponse.builder()
            .id(group.getId())
            .title(group.getTitle())
            .description(group.getExplanation())
            .period(group.getStudyStartDate().format(formatter) + " ~ " + group.getStudyEndDate().format(formatter))
            .recruitmentPeriod(group.getRecruitStartDate().format(formatter) + " ~ " + group.getRecruitEndDate().format(formatter))
            .tags(techTags)
            .maxMembers(group.getGroupMemberCount())
            .currentMembers(group.getMembers().size())
            .groupStatus(group.getGroupStatus())
            .organizer(
                    OrganizerDto.builder()
                            .name(creator.getNickname())
                            .avatar("/placeholder.svg?height=40&width=40")
                            .build()
            )
            .isOnline(!group.getIsOffline())
            .addresses(addresses)
            .build();
  }
}
