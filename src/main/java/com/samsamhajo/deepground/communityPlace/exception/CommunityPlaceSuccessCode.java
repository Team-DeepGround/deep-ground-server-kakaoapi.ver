package com.samsamhajo.deepground.communityPlace.exception;

import com.samsamhajo.deepground.global.success.SuccessCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommunityPlaceSuccessCode implements SuccessCode {

    REVIEW_CREATED(HttpStatus.CREATED, "리뷰가 정상적으로 생성되었습니다."),
    COMMUNITYPLACE_SUCCESS_SELECT(HttpStatus.OK, "스터디 장소가 성공적으로 조회됐습니다"),
    COMMUNITY_PLACE_SUCCESS_SEARCH(HttpStatus.OK, "해당 장소의 리뷰가 성공적으로 조회됐습니다."),
    COMMUNITYPLACE_SUCCESS_SELECT_BY_REVIEW_COUNT(HttpStatus.OK, "리뷰순으로 스터디 장소가 성공적으로 조회됐습니다" ),
    COMMUNITYPLACE_SUCCESS_SELECT_BY_REVIEW_SCOPE(HttpStatus.OK, "별점순으로 스터디 장소가 성공적으로 조회됐습니다" ),
    COMMUNITY_PLACE_SUCCESS_REVIEW_DETAIL(HttpStatus.OK, "리뷰 상세보기가 성공적으로 조회됐습니다."),
    COMMUNITY_PLACE_SUCCESS_REVIEW_MODIFIED(HttpStatus.OK, "리뷰가 성공적으로 수정되었습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
  
    CommunityPlaceSuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }


}
