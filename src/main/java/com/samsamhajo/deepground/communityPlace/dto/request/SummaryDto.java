package com.samsamhajo.deepground.communityPlace.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class SummaryDto {

    private Long communityPlaceReviewId;
    private double scope;
    private String content;
    private Long specificAddressId;
    private List<String> mediaUrl;

    public SummaryDto(Long communityPlaceReviewId, double scope, String content, Long specificAddressId, List<String> mediaUrl) {
        this.communityPlaceReviewId = communityPlaceReviewId;
        this.scope = scope;
        this.content = content;
        this.specificAddressId = specificAddressId;
        this.mediaUrl = mediaUrl;
    }

    public static SummaryDto of(Long communityPlaceReviewId, double scope, String content, Long specificAddressId, List<String> mediaUrl) {
        return new SummaryDto(communityPlaceReviewId, scope, content, specificAddressId, mediaUrl);
    }
}
