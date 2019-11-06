package org.psc.playground.controller;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@RestController
@RequestMapping("redirect")
public class RedirectController {

    @Bean("sslRestTemplate")
    public RestTemplate sslRestTemplate() throws NoSuchAlgorithmException, IOException, CertificateException,
            KeyStoreException, KeyManagementException {
        Resource trustStoreResource = new ClassPathResource("cacerts");

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(trustStoreResource.getInputStream(), "changeit".toCharArray());
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, TrustAllStrategy.INSTANCE).build();

        HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();

        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(httpComponentsClientHttpRequestFactory);
    }


    @GetMapping("posts")
    public ResponseEntity<String> getRedirect() throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException {
        return sslRestTemplate().getForEntity("https://jsonplaceholder.typicode.com/posts", String.class);
    }
}
