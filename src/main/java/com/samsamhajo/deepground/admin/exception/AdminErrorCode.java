package com.samsamhajo.deepground.admin.exception;


import com.samsamhajo.deepground.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AdminErrorCode implements ErrorCode {

    INVALID_BAN_DAYS(HttpStatus.BAD_REQUEST, "정지 기간은 1일 이상이어야 합니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return "[ADMIN ERROR] " + message;
    }
}
