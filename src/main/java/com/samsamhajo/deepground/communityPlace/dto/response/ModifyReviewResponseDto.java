package com.samsamhajo.deepground.communityPlace.dto.response;

import com.samsamhajo.deepground.communityPlace.dto.request.ModifyReviewDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ModifyReviewResponseDto {

    private Long communityPlaceReviewId;
    private Long memberId;
    private double scope;
    private String content;
    private Long specificAddressId;
    private List<String> mediaUrl;

    public ModifyReviewResponseDto(Long communityPlaceReviewId, Long memberId, double scope ,String content, Long specificAddressId, List<String> mediaUrl) {
        this.communityPlaceReviewId = communityPlaceReviewId;
        this.memberId = memberId;
        this.scope = scope;
        this.content = content;
        this.specificAddressId = specificAddressId;
        this.mediaUrl = mediaUrl;
    }
    public static ModifyReviewResponseDto of(Long communityPlaceReviewId, Long memberId, double scope ,String content, Long specificAddressId, List<String> mediaUrl) {
        return new ModifyReviewResponseDto(communityPlaceReviewId, memberId, scope ,content, specificAddressId, mediaUrl);
    }
}
