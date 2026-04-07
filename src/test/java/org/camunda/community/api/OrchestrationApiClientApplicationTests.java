package org.camunda.community.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "camunda.cluster.id=00000000-0000-0000-0000-000000000000",
        "camunda.cluster.region=bru-2",
        "camunda.client.id=test-client",
        "camunda.client.secret=test-secret"
})
class OrchestrationApiClientApplicationTests {

    @Test
    void contextLoads() {
    }

}
