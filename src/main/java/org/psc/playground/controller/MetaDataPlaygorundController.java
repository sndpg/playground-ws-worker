package org.psc.playground.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("metadata")
public class MetaDataPlaygorundController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String getMetadata(@RequestHeader MultiValueMap<String, String> requestHeaders,
            UriComponentsBuilder uriComponentsBuilder, HttpServletRequest request) {
        requestHeaders.forEach((k, v) -> log.info("{} -> {}", k, v));

        return String.format("""
                        {
                            "id" : 0,
                            "data" : "none",
                            "_link" : {
                                "host": "%s",
                                "from" : "%s",
                                "pathInfo": "%s",
                                "pathTranslated": "%s",
                                "servletPath": "%s"
                            }
                        }
                        """, requestHeaders.get("host"), uriComponentsBuilder.toUriString(),
                request.getPathInfo(),
                request.getPathTranslated(),
                request.getServletPath());
    }

}
