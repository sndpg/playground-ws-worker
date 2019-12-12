package org.psc.playground.controller;

import lombok.RequiredArgsConstructor;
import org.psc.playground.logic.MiscLogic;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.ReplayProcessor;

import java.util.Collections;
import java.util.Map;

/**
 * Controller for misc stuff
 */
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("service/misc")
public class MiscController {

    private final MiscLogic miscLogic;

    @Qualifier("echoParameters")
    private final ReplayProcessor<String> echoParameters;

    /**
     * Get status
     *
     * @return
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getMisc() {
        return Collections.singletonMap("status", "OK");
    }

    /**
     * Always return an exception
     *
     * @return
     * @throws Exception
     */
    @PreAuthorize("oauth2.hasAnyScope('read','write')")
    @GetMapping(path = "exception")
    public Map<String, String> getException() throws Exception {
        String status = miscLogic.getException();
        return Map.of("status", status);
    }

    /**
     * Echoes the request
     *
     * @param value
     * @return
     */
    @GetMapping(path = "echo")
    public String echo(@RequestParam String value) {
        echoParameters.onNext(value + "\n");
        return value;
    }

    /**
     * Returns all echoed parameters as a Publisher.
     *
     * @return
     */
    @GetMapping(path = "echos", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Publisher<String> echos() {
        return echoParameters.log();
    }

    /**
     * Echoes the {@link ResponseBody}, does nothing otherwise.
     *
     * @param body
     * @return
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_XML_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> postMisc(@RequestBody Map<String, Object> body) {
        return body;
    }

}
