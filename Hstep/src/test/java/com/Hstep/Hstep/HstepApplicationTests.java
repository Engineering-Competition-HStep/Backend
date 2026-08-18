package com.Hstep.Hstep;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.ai-roadmap-standard-seed.enabled=false",
        "app.notice-crawler.enabled=false"
})
class HstepApplicationTests {

    @Test
    void contextLoads() {
    }
}
