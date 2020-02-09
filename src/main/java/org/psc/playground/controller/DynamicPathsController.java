package org.psc.playground.controller;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("dynamic")
public class DynamicPathsController {

    private Map<ResponseConfigKey, ResponseConfig> responseConfigMap = new ConcurrentHashMap<>(100);

    @GetMapping(path = "/{path}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMapping(@PathVariable String path,
            @RequestParam LinkedMultiValueMap<String, Object> requestParameters) {

        requestParameters.forEach((key, value) -> log.info("{}: {}", key, value));
        ResponseConfig responseConfig =
                responseConfigMap.get(ResponseConfigKey.builder().method(HttpMethod.GET).path(path).build());

        return ResponseEntity.status(responseConfig.httpStatus).body(responseConfig.content);
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> registerMapping(@RequestBody Map<String, Object> requestBody) {
        ResponseConfigKey responseConfigKey = ResponseConfigKey.builder()
                .method(HttpMethod.valueOf((String) requestBody.getOrDefault("method", "GET")))
                .path(getUniqueMappingKey())
                .build();

        ResponseConfig responseConfig = ResponseConfig.builder()
                .content((String) requestBody.getOrDefault("response", ""))
                .httpStatus(HttpStatus.resolve(Integer.parseInt((String) requestBody.getOrDefault("httpStatus", 200))))
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
        private String content;
        private HttpStatus httpStatus;
    }

}
