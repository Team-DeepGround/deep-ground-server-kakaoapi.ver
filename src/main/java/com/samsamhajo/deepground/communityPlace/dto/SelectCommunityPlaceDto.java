package com.samsamhajo.deepground.communityPlace.dto;

import com.samsamhajo.deepground.communityPlace.entity.SpecificAddress;
import lombok.Getter;

@Getter
public class SelectCommunityPlaceDto {

    private Long id;
    private String name;
    private String phone;
    private String location;
    private double latitude;
    private double longitude;

    private SelectCommunityPlaceDto(Long id, String name, String phone, String location, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static SelectCommunityPlaceDto of(SpecificAddress specificAddress){
        return new SelectCommunityPlaceDto(
                specificAddress.getId(),
                specificAddress.getName(),
                specificAddress.getPhone(),
                specificAddress.getLocation(),
                specificAddress.getLatitude(),
                specificAddress.getLongitude()
        );
    }

}
