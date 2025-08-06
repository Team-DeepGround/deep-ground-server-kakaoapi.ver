package com.samsamhajo.deepground.report.exception;


import com.samsamhajo.deepground.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {

    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND,"신고를 찾을 수 없습니다."),
    INVALID_TYPE(HttpStatus.BAD_REQUEST, "올바른 타입이 아닙니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return "[REPORT ERROR] " + message;
    }
}

