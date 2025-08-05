package com.samsamhajo.deepground.communityPlace.dto.request;

import com.samsamhajo.deepground.communityPlace.entity.CommunityPlaceReview;
import lombok.Getter;

import java.util.List;

@Getter
public class SearchReviewSummaryDto {

    private Long reviewId;
    private double scope;
    private String content;
    private List<String> mediaUrl;

    public SearchReviewSummaryDto(Long reviewId, double scope, String content, List<String> mediaUrl) {
        this.reviewId = reviewId;
        this.scope = scope;
        this.content = content;
        this.mediaUrl = mediaUrl;
    }
    public static SearchReviewSummaryDto of(CommunityPlaceReview c, List<String> mediaUrl) {
        return new SearchReviewSummaryDto(c.getId(), c.getScope(), c.getContent(), mediaUrl);
    }

}
