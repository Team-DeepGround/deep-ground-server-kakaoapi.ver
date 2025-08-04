package com.samsamhajo.deepground.studyGroup.dto;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyGroupUpdateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "설명은 필수입니다.")
    private String explanation;

    @NotNull(message = "스터디 시작일은 필수입니다.")
    private LocalDate studyStartDate;

    @NotNull(message = "스터디 종료일은 필수입니다.")
    private LocalDate studyEndDate;

    @NotNull(message = "모집 시작일은 필수입니다.")
    private LocalDate recruitStartDate;

    @NotNull(message = "모집 종료일은 필수입니다.")
    private LocalDate recruitEndDate;

    @NotNull(message = "정원은 필수입니다.")
    @Min(value = 1, message = "정원은 최소 1명 이상이어야 합니다.")
    private Integer groupMemberCount;

    @NotNull(message = "오프라인 여부는 필수입니다.")
    private Boolean isOffline;

    private String studyLocation;

    @Size(min = 1, message = "최소 1개 이상의 기술 스택을 선택해주세요.")
    private List<String> techStackNames;

    private List<Long> addressIds;
}
