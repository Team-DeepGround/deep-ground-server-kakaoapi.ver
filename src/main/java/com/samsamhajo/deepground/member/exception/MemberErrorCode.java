package com.samsamhajo.deepground.member.exception;

import com.samsamhajo.deepground.global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."),
    INVALID_MEMBER_EMAIL(HttpStatus.BAD_REQUEST, "찾을 수 없는 사용자입니다"),
    INVALID_MEMBER_ID(HttpStatus.BAD_REQUEST, "입력하신 회원 ID가 올바르지 않거나 존재하지 않습니다."),
    MEMBER_NOT_IN_STUDY_GROUP(HttpStatus.BAD_REQUEST,"해당 스터디 그룹원이 아닙니다.");


    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return "[MEMBER ERROR]" + message;
    }
}
