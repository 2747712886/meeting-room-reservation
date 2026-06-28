package com.example.reservation.meetingroom.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

public record PageResponse<T>(
        long total,
        long page,
        long size,
        List<T> records
) {

    public static <T> PageResponse<T> from(IPage<?> page, List<T> records) {
        return new PageResponse<>(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                records
        );
    }
}

