package org.psc.playground.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("dynamic")
public class DynamicPathsController {

    private final ObjectMapper objectMapper;

    private final Map<ResponseConfigKey, ResponseConfig> responseConfigMap = new ConcurrentHashMap<>(100);

    @GetMapping(path = "/{path}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getMapping(@PathVariable String path,
            @RequestParam MultiValueMap<String, Object> requestParams) throws JsonProcessingException {

        requestParams.forEach((key, value) -> log.info("{}: {}", key, value));
        ResponseConfig responseConfig =
                responseConfigMap.get(ResponseConfigKey.builder().method(HttpMethod.GET).path(path).build());

        return ResponseEntity.status(responseConfig.getHttpStatus())
                .body(resolveContent(requestParams, responseConfig.getContent()));
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> registerMapping(@RequestBody Map<String, Object> requestBody) {
        ResponseConfigKey responseConfigKey = ResponseConfigKey.builder()
                .method(HttpMethod.valueOf((String) requestBody.getOrDefault("method", "GET")))
                .path(getUniqueMappingKey())
                .build();

        // list needs to be mutable
        @SuppressWarnings({"ArraysAsListWithZeroOrOneArgument", "unchecked"})
        Map<String, List<Object>> requestParams =
                ((Map<String, Object>) requestBody.getOrDefault("requestParams", new ConcurrentHashMap<>())).entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.asList(entry.getValue())
                                , (a, b) -> {
                                    a.addAll(b);
                                    return a;
                                }));

        ResponseConfig responseConfig = ResponseConfig.builder()
                .content((String) requestBody.getOrDefault("response", ""))
                .httpStatus(HttpStatus.resolve(Integer.parseInt((String) requestBody.getOrDefault("httpStatus", 200))))
                .requestParams(new LinkedMultiValueMap<>(requestParams))
                .build();

        responseConfigMap.put(responseConfigKey, responseConfig);

        Map<String, String> response = Map.of("path", "/dynamic/" + responseConfigKey.getPath(),
                "method", responseConfigKey.method.toString());
        return ResponseEntity.ok(response);
    }

    private String getUniqueMappingKey() {
        String generatedKey;
        List<String> keyContainer = new ArrayList<>();
        do {
            generatedKey = RandomStringUtils.randomAlphabetic(8);
            keyContainer.clear();
            keyContainer.add(0, generatedKey);
        } while (responseConfigMap.keySet()
                .stream()
                .anyMatch(key -> key.getPath().equals(keyContainer.get(0))));

        return generatedKey;
    }

    private Object resolveContent(MultiValueMap<String, Object> requestParams, String responseContent) throws
            JsonProcessingException {
        Map<String, Object> responseContentTree = objectMapper.readValue(responseContent,
                new TypeReference<>() {});

        ExpressionParser expressionParser = new SpelExpressionParser();
        EvaluationContext evaluationContext = SimpleEvaluationContext.forReadWriteDataBinding().build();

        evaluationContext.setVariable("requestParams", requestParams);

        resolveExpression(responseContentTree.entrySet(), expressionParser, evaluationContext);

        return objectMapper.writeValueAsString(responseContentTree);
    }

    /**
     * Resolves a simple SpEL-expression within the responseContentEntries, which allows to reference requestParams of
     * the current request within the given evaluationContext ({@code requestParams}-Map has to be set as a variable in
     * the evaluationContext).
     *
     * @param responseContentEntries the entries of the response which has been registered on this path and potentially
     *                               contains simple SpEL-expressions which can reference values from the requestParams
     *                               of the current request
     * @param expressionParser       for parsing expressions
     * @param evaluationContext      evaluationContext in which the variable {@code requestParams} has been set with the
     *                               values of the current requestParams
     */
    private void resolveExpression(Set<Map.Entry<String, Object>> responseContentEntries,
            ExpressionParser expressionParser, EvaluationContext evaluationContext) {
        for (Map.Entry<String, Object> responseContentEntry : responseContentEntries) {
            //noinspection rawtypes
            if (responseContentEntry.getValue() instanceof Map subEntries) {
                //noinspection unchecked
                resolveExpression(subEntries.entrySet(), expressionParser, evaluationContext);
            } else if (responseContentEntry.getValue() instanceof String currentValue &&
                    currentValue.startsWith("#")) {
                //noinspection unchecked
                String resolvedExpression =
                        ((List<String>) Objects.requireNonNull(expressionParser
                                .parseExpression(currentValue)
                                .getValue(evaluationContext)))
                                .get(0);

                // for now, just create a Number and let jackson figure out which actual type to write into the
                // response
                if (NumberUtils.isCreatable(resolvedExpression)) {
                    responseContentEntry.setValue(NumberUtils.createNumber(resolvedExpression));
                } else {
                    responseContentEntry.setValue(resolvedExpression);
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @With
    @Builder
    private static class ResponseConfigKey {
        private String path;
        private HttpMethod method;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @With
    @Builder
    private static class ResponseConfig {
        private MultiValueMap<String, Object> requestParams;
        private String content;
        private HttpStatus httpStatus;
    }

}
