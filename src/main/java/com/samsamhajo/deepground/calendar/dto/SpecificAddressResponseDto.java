package com.samsamhajo.deepground.calendar.dto;

import com.samsamhajo.deepground.communityPlace.entity.SpecificAddress;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpecificAddressResponseDto {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String placeUrl;
    private Double latitude;
    private Double longitude;

    public static SpecificAddressResponseDto from(SpecificAddress specificAddress) {
        return SpecificAddressResponseDto.builder()
                .id(specificAddress.getId())
                .name(specificAddress.getName())
                .address(specificAddress.getLocation())
                .phone(specificAddress.getPhone())
                .placeUrl(specificAddress.getPlaceUrl())
                .latitude(specificAddress.getLatitude())
                .longitude(specificAddress.getLongitude())
                .build();
    }
}
