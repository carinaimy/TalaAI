package com.tala.core.exception;

import lombok.Getter;

/**
 * Base exception for all Tala exceptions
 * 
 * @author Tala Backend Team
 */
@Getter
public class TalaException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final transient Object[] args;
    
    public TalaException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public TalaException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public TalaException(ErrorCode errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public TalaException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public TalaException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public int getStatusCode() {
        return errorCode.getCode();
    }
}
