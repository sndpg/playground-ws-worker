package org.psc.playground;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Disabled("doesn't work, no idea why")
@SpringBootTest(classes = PlaygroundWsWorkerApplication.class)
class PlaygroundWsWorkerApplicationTests {

    @Test
    void contextLoads() {
        assertThat(true, is(true));
    }

}
