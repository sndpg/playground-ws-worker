package org.psc.playground.controller;

import io.woof.database.QueryResolver;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.psc.playground.configuration.WorkerBeanDefinitionRegistryPostProcessor;
import org.psc.playground.logic.MiscLogic;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.ReplayProcessor;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for misc stuff
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("service/misc")
public class MiscController {

    private final MiscLogic miscLogic;

    private final QueryResolver queryResolver;

    private final WorkerBeanDefinitionRegistryPostProcessor.Oi oi;

    @PostConstruct
    public void postConstruct(){
        oi.doSomething();
    }

    @Qualifier("echoParameters")
    private final ReplayProcessor<String> echoParameters;

    @GetMapping("queries")
    public ResponseEntity<Map<String, String>> getQuery(@RequestParam String queryName) {
        return ResponseEntity.of(queryResolver.getStatement(queryName).map(query -> Map.of(queryName, query)));
    }

    /**
     * Get status
     *
     * @return
     */
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
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
     * @param headers
     * @param value
     * @return
     */
    @GetMapping(path = "echo")
    public Map<String, Object> echo(@RequestHeader Map<String, String> headers, @RequestParam String value) {
        echoParameters.onNext(value + "\n");

        log.info("request param: {}", value);

        if (log.isDebugEnabled()) {
            log.debug("request headers:");
            headers.forEach((headerKey, headerValue) -> log.debug("{}: {}", headerKey, headerValue));
        }

        Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId;
        if (principalObject instanceof Principal principal) {
            userId = principal.getName();
        } else {
            userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        return Map.of("userId", userId,
                "value", value,
                "headers", headers);
    }

    //    /**
    //     * GETs an arbitraty {@link playground.domain.Information}-Object
    //     *
    //     * @return
    //     */
    //    @GetMapping(path = "information")
    //    public Information getInformation() {
    //        return new Information(new Random().nextLong(), "info", "empty");
    //    }

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


    /**
     * Complex type extracted from request parameters.
     *
     * @param miscDtoAttributes
     * @return
     */
    @GetMapping(path = "dto")
    public MiscDto getDto(@RequestParam Map<String, String> miscDtoAttributes) {
        return MiscDto.builder()
                .id(miscDtoAttributes.get("id"))
                .description(miscDtoAttributes.get("description"))
                .status(Integer.parseInt(miscDtoAttributes.get("status")))
                .value(BigDecimal.valueOf(Double.parseDouble(miscDtoAttributes.get("value")))).build();
    }

    @PostMapping("locations")
    public ResponseEntity<Map<String, Object>> postLocation(@RequestBody Map<String, Object> location) {
        URI locationUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{locationId}")
                .build(location.get("id"));
        // scenario for Optional mapping
        return Optional.of(location)
                .map(body -> ResponseEntity.created(locationUri).body(body))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class MiscDto {
        private String id;
        private String description;
        private int status;
        private BigDecimal value;
    }
}
