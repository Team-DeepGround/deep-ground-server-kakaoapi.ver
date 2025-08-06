package com.samsamhajo.deepground.report.exception;

import com.samsamhajo.deepground.global.error.core.BaseException;
import com.samsamhajo.deepground.global.error.core.ErrorCode;

public class ReportException extends BaseException {

    public ReportException(ErrorCode errorCode) {
        super(errorCode);
    }
}
