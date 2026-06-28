package com.example.reservation;

import java.time.LocalDateTime;
import java.util.Map;
import com.example.reservation.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "reservation-backend",
                "time", LocalDateTime.now()
        ));
    }
}
