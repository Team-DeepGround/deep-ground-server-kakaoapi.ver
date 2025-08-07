package com.samsamhajo.deepground.report.success;

import com.samsamhajo.deepground.global.success.SuccessCode;
import org.springframework.http.HttpStatus;

public enum ReportSuccessCode implements SuccessCode {
    CREATE_SUCCESS(HttpStatus.CREATED, "신고에 성공했습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ReportSuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
