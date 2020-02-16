package org.psc.playground.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("jackson")
public class JacksonPlaygroundController {

    private final ObjectMapper objectMapper;
    private final ObjectMapper snakeCasePropertiesObjectMapper;
    private final ObjectMapper customPropertiesObjectMapper;

    public JacksonPlaygroundController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        snakeCasePropertiesObjectMapper = objectMapper.copy()
                .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        customPropertiesObjectMapper = objectMapper.copy()
                .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                .setPropertyNamingStrategy(new CustomMiscDtoPropertyNamingStrategy());
    }

    /**
     * Requires input properties to be provided in snake case (e.g. {@code user_name}
     *
     * @param body input properties
     * @return MiscDto
     * @throws JsonProcessingException Jackson
     */
    @PostMapping(value = "properties/misc/snakecase", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public MiscDto postMiscDtoWithSnakeCasePropertiesFromRawJson(@RequestBody String body) throws
            JsonProcessingException {
        return snakeCasePropertiesObjectMapper.readValue(body, MiscDto.class);
    }

    /**
     * Requires input properties to be derivable by jackson's default {@link PropertyNamingStrategy} (actual names of
     * the member variables of a class).
     *
     * @param body input properties
     * @return MiscDto
     * @throws JsonProcessingException Jackson
     */
    @PostMapping(value = "properties/misc/default", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public MiscDto postMiscDtoFromRawJson(@RequestBody String body) throws JsonProcessingException {
        return objectMapper.readValue(body, MiscDto.class);
    }

    /**
     * @param body
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping(value = "properties/misc/customized", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public MiscDto postMiscDtoFromCustomizedRawJson(@RequestBody String body) throws JsonProcessingException {
        return customPropertiesObjectMapper.readValue(body, MiscDto.class);
    }

    private static class CustomMiscDtoPropertyNamingStrategy extends PropertyNamingStrategy.PropertyNamingStrategyBase {

        private final static Map<String, String> TRANSLATED_NAMES = Map.of("id", "entityId",
                "userName", "userId",
                "value", "openAmount",
                "secondaryValue", "payedAmount",
                "text", "text",
                "date", "date");

        public String translate(String propertyName) {
            return TRANSLATED_NAMES.getOrDefault(propertyName, propertyName);
        }

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @With
    @Builder
    private static class MiscDto {
        private String id;

        private String userName;

        @JsonProperty("value")
        private BigDecimal numericValue;

        @JsonProperty("secondaryValue")
        private BigDecimal secondaryNumericValue;

        private String text;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate date;
    }
}
