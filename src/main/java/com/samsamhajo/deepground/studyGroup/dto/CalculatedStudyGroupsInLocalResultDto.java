package com.samsamhajo.deepground.studyGroup.dto;

import lombok.Getter;


@Getter
public class CalculatedStudyGroupsInLocalResultDto {
    Long count;
    Long addressId;

    public CalculatedStudyGroupsInLocalResultDto(Long count, Long addressId) {
        this.count = count;
        this.addressId = addressId;
    }
}
