package com.samsamhajo.deepground.communityPlace.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReviewStatistics {

    private double avgScope;
    private Long reviewCount;

    public ReviewStatistics (double avgScope,Long reviewCount) {
        this.avgScope = avgScope;
        this.reviewCount = reviewCount;
    }

    public static ReviewStatistics of(double avgScope,Long reviewCount) {
        return new ReviewStatistics(avgScope,reviewCount);
    }
}
