package com.samsamhajo.deepground.communityPlace.dto;

import com.samsamhajo.deepground.communityPlace.dto.response.ReviewResponseDto;
import com.samsamhajo.deepground.communityPlace.entity.CommunityPlaceReview;
import lombok.Builder;
import lombok.Getter;

import java.awt.*;

@Getter
@Builder
public class CommunityPlaceReviewDto {

    private Long id;
    private String name;
    private double avgScope;
    private Long countContent;
    private Long phone;

    public static CommunityPlaceReviewDto from(CommunityPlaceReview review) {
        return CommunityPlaceReviewDto.builder()
                .id(review.getId())
                .avgScope(review.getScope())
                .countContent(review.getContent())
                .build();
    }

}
