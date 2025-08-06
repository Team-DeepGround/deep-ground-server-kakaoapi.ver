package com.samsamhajo.deepground.admin.exception;


import com.samsamhajo.deepground.global.error.core.BaseException;
import com.samsamhajo.deepground.global.error.core.ErrorCode;

public class AdminException extends BaseException {

    public AdminException(ErrorCode errorCode) {
        super(errorCode);
    }
}

