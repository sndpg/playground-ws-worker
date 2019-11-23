package org.psc.playground.controller;

import lombok.RequiredArgsConstructor;
import org.psc.playground.logic.MiscLogic;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("misc")
public class MiscController {

    private final MiscLogic miscLogic;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getMisc() {
        return Collections.singletonMap("status", "OK");
    }

    @GetMapping(path = "exception")
    public Map<String, String> getException() throws Exception {
        String status = miscLogic.getException();
        return Map.of("status", status);
    }

    @GetMapping(path = "echo")
    public String echo(@RequestParam String value){
        return value;
    }

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> postMisc(@RequestBody Map<String, Object> body) {
        return body;
    }
}
