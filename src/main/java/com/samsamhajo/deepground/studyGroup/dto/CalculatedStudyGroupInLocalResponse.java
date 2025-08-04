package com.samsamhajo.deepground.studyGroup.dto;

import com.samsamhajo.deepground.address.dto.AddressDto;
import lombok.Getter;


@Getter
public class CalculatedStudyGroupInLocalResponse {
    Long count;
    AddressDto address;

    private CalculatedStudyGroupInLocalResponse(Long count, AddressDto address) {
        this.count = count;
        this.address = address;
    }

    public static CalculatedStudyGroupInLocalResponse of(Long count, AddressDto address) {
        return new CalculatedStudyGroupInLocalResponse(count, address);
    }
}
