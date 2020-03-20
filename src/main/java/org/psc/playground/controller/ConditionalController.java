package org.psc.playground.controller;

import org.psc.playground.conditional.ConditionalOnDate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnDate("2020-03-19")
public class ConditionalController {

    @GetMapping(value = "/conditions", produces = MediaType.APPLICATION_JSON_VALUE)
    public String geConditions() {
        return """
                {
                    "condition": "ok"
                }
                """;
    }
}
