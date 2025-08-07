package com.samsamhajo.deepground.communityPlace.dto;

public interface SelectCommunityPlace {
    Long getId();
    String getLocation();
    String getName();
    String getPhone();
    String getPlaceUrl();
    Double getLatitude();
    Double getLongitude();
    Double getAvgScope();
    Long getCountReview();
}
