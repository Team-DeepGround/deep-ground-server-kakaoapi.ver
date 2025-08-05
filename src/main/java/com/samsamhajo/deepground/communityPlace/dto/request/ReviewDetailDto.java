package com.samsamhajo.deepground.communityPlace.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class ReviewDetailDto {

    private Long communityPlaceReviewId;
    private String content;
    private String nickname;
    private double scope;
    private Long memberId;
    private List<String> mediaUrl;

    public ReviewDetailDto(Long communityPlaceReviewId, String content, String nickname, double scope, Long memberId, List<String> mediaUrl) {
        this.communityPlaceReviewId = communityPlaceReviewId;
        this.content = content;
        this.nickname = nickname;
        this.scope = scope;
        this.memberId = memberId;
        this.mediaUrl = mediaUrl;
    }

    public static ReviewDetailDto of(Long communityPlaceReviewId, String content, String nickname,double scope, Long memberId, List<String> mediaUrl) {
        return new ReviewDetailDto(communityPlaceReviewId, content, nickname, scope, memberId, mediaUrl);
    }
}
