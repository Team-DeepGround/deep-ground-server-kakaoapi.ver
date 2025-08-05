package com.samsamhajo.deepground.communityPlace.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ReviewStatistics {

    private double avgScope;
    private Long countContent;

    public ReviewStatistics (double avgScope,Long countContent) {
        this.avgScope = avgScope;
        this.countContent = countContent;
    }

    public static ReviewStatistics of(double avgScope,Long countContent) {
        return new ReviewStatistics(avgScope,countContent);
    }
}
