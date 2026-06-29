package com.example.reservation.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;

@JsonTest
@Import(JacksonConfig.class)
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializesLongAsStringToAvoidJavaScriptPrecisionLoss() throws Exception {
        String json = objectMapper.writeValueAsString(new LongPayload(2071118740789719042L));

        assertThat(json).contains("\"id\":\"2071118740789719042\"");
    }

    record LongPayload(Long id) {
    }
}
