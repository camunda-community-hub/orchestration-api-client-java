package org.camunda.community.api.soap;

import org.camunda.community.api.DecisionEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.Source;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.noFault;
import static org.springframework.ws.test.server.ResponseMatchers.payload;

@SpringJUnitConfig(classes = {
        SoapWebServiceConfig.class,
        DecisionEvaluationSoapEndpoint.class,
        DecisionEvaluationSoapEndpointContractTest.MockConfig.class
})
class DecisionEvaluationSoapEndpointContractTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DecisionEvaluationService decisionEvaluationService;

    private MockWebServiceClient client;

    @BeforeEach
    void setUp() {
        client = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    void evaluateDecisionSoapContractSuccess() {
        given(decisionEvaluationService.evaluate(any())).willReturn(Map.of("outcome", "approved"));

        Source request = new StringSource("""
                <ns:evaluateDecisionRequest xmlns:ns="http://camunda.org/consulting/decision-evaluation">
                    <ns:decisionDefinitionId>decision-id-1</ns:decisionDefinitionId>
                    <ns:variables>
                        <ns:entry>
                            <ns:key>amount</ns:key>
                            <ns:value>100</ns:value>
                        </ns:entry>
                    </ns:variables>
                </ns:evaluateDecisionRequest>
                """);

        // Contract response is JAXB XML: assert the structured result element instead of JSON text.
        Source expected = new StringSource("""
                <ns:evaluateDecisionResponse xmlns:ns="http://camunda.org/consulting/decision-evaluation">
                    <ns:success>true</ns:success>
                    <ns:result>
                        <outcome>approved</outcome>
                    </ns:result>
                </ns:evaluateDecisionResponse>
                """);

        client.sendRequest(withPayload(request))
                .andExpect(noFault())
                .andExpect(payload(expected));

        verify(decisionEvaluationService).evaluate(any());
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        DecisionEvaluationService decisionEvaluationService() {
            return Mockito.mock(DecisionEvaluationService.class);
        }
    }
}
