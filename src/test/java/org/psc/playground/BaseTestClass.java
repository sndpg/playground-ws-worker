package org.psc.playground;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.psc.playground.controller.MiscController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class BaseTestClass {

    @Autowired
    private MiscController miscController;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.standaloneSetup(miscController);
    }
}
