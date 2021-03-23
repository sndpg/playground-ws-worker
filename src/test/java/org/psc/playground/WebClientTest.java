package org.psc.playground;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WebClientTest {

    @Test
    void test() {
        WebClient webClient = WebClient.builder()
                .codecs(clientCodecConfigurer ->
                        clientCodecConfigurer.customCodecs()
                                .registerWithDefaultConfig(
                                        new InspectingJsonEncoder(new ObjectMapper(),
                                                MimeType.valueOf(MimeTypeUtils.APPLICATION_JSON_VALUE))))
                .build();

        Map<String, Object> body = Map.of("key", "value");

        webClient.post().uri("http://localhost:8080/misc")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private static class InspectingJsonEncoder extends Jackson2JsonEncoder {
        public InspectingJsonEncoder(ObjectMapper mapper,
                MimeType... mimeTypes) {
            super(mapper, mimeTypes);
        }

        @Override
        public Flux<DataBuffer> encode(Publisher<?> inputStream, DataBufferFactory bufferFactory,
                ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
            return super.encode(inputStream, bufferFactory, elementType, mimeType, hints);
        }

        @Override
        public DataBuffer encodeValue(Object value, DataBufferFactory bufferFactory, ResolvableType valueType,
                MimeType mimeType, Map<String, Object> hints) {
            // noinspection unchecked
            assertThat((Map<String, String>) value).containsEntry("key", "value");
            return super.encodeValue(value, bufferFactory, valueType, mimeType, hints);
        }
    }
}
