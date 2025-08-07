package com.samsamhajo.deepground.admin.success;

import com.samsamhajo.deepground.global.success.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminSuccessCode implements SuccessCode {
    // 대시보드 조회 성공
    GET_DASHBOARD_SUCCESS(HttpStatus.OK, "대시보드 조회 성공"),
    GET_REPORT_SUCCESS(HttpStatus.OK, "신고 조회 성공"),
    BAN_MEMBER_SUCCESS(HttpStatus.OK, "회원 정지 설정 완료"),
    GET_REPORT_DETAIL_SUCCESS(HttpStatus.OK, "신고 상세 조회 성공")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[ADMIN] " + message;
    }
}
