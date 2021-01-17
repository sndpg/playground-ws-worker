package org.psc.playground.controller;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vavr.control.Try;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;

@RestController
@CrossOrigin
@RequestMapping("jackson")
public class JacksonPlaygroundController {

    private final ObjectMapper objectMapper;
    private final ObjectMapper snakeCasePropertiesObjectMapper;
    private final ObjectMapper customPropertiesObjectMapper;

    private final Resource persons;

    public JacksonPlaygroundController(ObjectMapper objectMapper,
            @Value("classpath:json-data/persons.json") Resource persons) {
        this.objectMapper = objectMapper;

        snakeCasePropertiesObjectMapper = objectMapper.copy()
                .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        customPropertiesObjectMapper = objectMapper.copy()
                .configure(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING, true)
                .setPropertyNamingStrategy(new CustomMiscDtoPropertyNamingStrategy());

        this.persons = persons;
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

    @PostMapping(value = "misc/escapes", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public String postWithEscapedCharacters(@RequestBody String body) throws JsonProcessingException {
        Map<String, Object> tree = objectMapper.readValue(body, new TypeReference<>() {});

        transformTree(tree);

        return objectMapper.writeValueAsString(tree);
    }

    /**
     * Get all persons saved within persons.json
     *
     * @return all persons within persons.json
     */
    @GetMapping(value = "records/persons")
    public List<Person> getPersons() {
        return Try.of(() -> objectMapper.readValue(persons.getInputStream(), new TypeReference<List<Person>>() {}))
                .getOrElseThrow((Function<Throwable, IllegalStateException>) IllegalStateException::new);
    }

    private void transformTree(Map<String, Object> tree) {
        for (Map.Entry<String, Object> current : tree.entrySet()) {
            transformNodes(current);
        }
    }

    private void transformNodes(Map.Entry<String, Object> current) {
        if (current.getValue() != null && current.getValue() instanceof String) {
            current.setValue(transformValue((String) current.getValue()));
        } else if (current.getValue() instanceof List) {
            @SuppressWarnings("unchecked") List<Object> currentValues = (List<Object>) current.getValue();
            for (int i = 0; i < currentValues.size(); i++) {
                if (currentValues.get(i) instanceof String) {
                    currentValues.set(i, transformValue((String) currentValues.get(i)));
                }
            }
        } else if (current.getValue() instanceof Map) {
            //noinspection unchecked
            transformTree((Map<String, Object>) current.getValue());
        }
    }

    private String transformValue(String value) {
        return "TEXT: " + value;
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

    public record Person(
            @JsonProperty("id") int id,
            @JsonProperty("firstname") String firstname,
            @JsonProperty("lastname") String lastname,
            @JsonProperty("score") OptionalInt score,
            @JsonProperty("emailAddresses") @JsonSetter(nulls = Nulls.AS_EMPTY) List<String> emailAddresses) {

        public Person {
            if (score.isEmpty()){
                score = OptionalInt.of(0);
            }
        }
    }

}
