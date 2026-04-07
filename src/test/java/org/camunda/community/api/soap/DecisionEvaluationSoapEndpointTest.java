package org.camunda.community.api.soap;

import org.camunda.community.api.DecisionDTO;
import org.camunda.community.api.DecisionEvaluationService;
import org.camunda.community.api.soap.model.EvaluateDecisionRequest;
import org.camunda.community.api.soap.model.EvaluateDecisionResponse;
import org.camunda.community.api.soap.model.SoapDecisionVariables;
import org.camunda.community.api.soap.model.SoapVariableEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DecisionEvaluationSoapEndpointTest {

    @Mock
    private DecisionEvaluationService decisionEvaluationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DecisionEvaluationSoapEndpoint endpoint;

    @Captor
    private ArgumentCaptor<DecisionDTO> dtoCaptor;

    @Test
    void evaluateDecisionMapsSoapRequestAndReturnsSerializedResult() throws Exception {
        given(decisionEvaluationService.evaluate(any())).willReturn(Map.of("outcome", "approved"));
        given(objectMapper.writeValueAsString(Map.of("outcome", "approved")))
                .willReturn("{\"outcome\":\"approved\"}");

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        request.setDecisionDefinitionId("decision-id-1");
        request.setDecisionDefinitionKey("2251799813326547");

        SoapDecisionVariables variables = new SoapDecisionVariables();
        variables.setEntries(List.of(
                new SoapVariableEntry("amount", 100),
                new SoapVariableEntry("country", "US")
        ));
        request.setVariables(variables);

        EvaluateDecisionResponse response = endpoint.evaluateDecision(request);

        assertTrue(response.isSuccess());
        assertEquals("{\"outcome\":\"approved\"}", response.getResult());
        assertNull(response.getErrorMessage());

        verify(decisionEvaluationService).evaluate(dtoCaptor.capture());
        DecisionDTO captured = dtoCaptor.getValue();
        assertEquals("decision-id-1", captured.getDecisionDefinitionId());
        assertEquals("2251799813326547", captured.getDecisionDefinitionKey());
        assertEquals(100, captured.getVariables().get("amount"));
        assertEquals("US", captured.getVariables().get("country"));
    }

    @Test
    void evaluateDecisionReturnsErrorWhenServiceThrows() {
        given(decisionEvaluationService.evaluate(any())).willThrow(new RuntimeException("service failure"));

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        request.setDecisionDefinitionId("decision-id-1");

        EvaluateDecisionResponse response = endpoint.evaluateDecision(request);

        assertFalse(response.isSuccess());
        assertEquals("service failure", response.getErrorMessage());
    }

    @Test
    void evaluateDecisionReturnsErrorWhenSerializationFails() throws Exception {
        Map<String, Object> result = Map.of("outcome", "approved");
        given(decisionEvaluationService.evaluate(any())).willReturn(result);
        given(objectMapper.writeValueAsString(result)).willThrow(new RuntimeException("json failure"));

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        request.setDecisionDefinitionKey("2251799813326547");

        EvaluateDecisionResponse response = endpoint.evaluateDecision(request);

        assertFalse(response.isSuccess());
        assertEquals("json failure", response.getErrorMessage());
    }
}

