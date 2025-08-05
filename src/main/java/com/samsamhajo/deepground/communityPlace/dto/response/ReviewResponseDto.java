package com.samsamhajo.deepground.communityPlace.dto.response;

import lombok.Getter;
import java.util.List;

@Getter
public class ReviewResponseDto {
    private Long id;
    private double scope;
    private String content;
    private Long memberId;
    private Long specificAddressId;
    private List<String> mediaUrls;

    public ReviewResponseDto(Long id, double scope, String content, Long memberId, Long specificAddressId,
                             List<String> mediaUrls) {
        this.id = id;
        this.scope = scope;
        this.content = content;
        this.memberId = memberId;
        this.specificAddressId = specificAddressId;
        this.mediaUrls = mediaUrls;
    }

    public static ReviewResponseDto of(Long id, double scope, String content, Long memberId, Long specificAddressId,
                                       List<String> mediaUrls) {
        return new ReviewResponseDto(id, scope, content, memberId, specificAddressId, mediaUrls);
    }
}
