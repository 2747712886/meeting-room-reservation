package com.example.reservation.common;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMessage(),
                data,
                LocalDateTime.now()
        );
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(
                errorCode.getCode(),
                message,
                null,
                LocalDateTime.now()
        );
    }
}

