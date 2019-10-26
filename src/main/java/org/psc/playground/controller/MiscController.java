package org.psc.playground.controller;

import lombok.RequiredArgsConstructor;
import org.psc.playground.DefaultExceptionHandlerAutoConfiguration;
import org.psc.playground.logic.MiscLogic;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("misc")
public class MiscController {

    private final MiscLogic miscLogic;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getMisc(){
        return Collections.singletonMap("status", "OK");
    }

    @GetMapping(path = "exception")
    public Map<String, String> getException() throws Exception {
        String status = miscLogic.getException();
        return Map.of("status", status);
    }
}
