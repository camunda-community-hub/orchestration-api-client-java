package org.camunda.community.api.soap;

import tools.jackson.databind.ObjectMapper;
import org.camunda.community.api.DecisionDTO;
import org.camunda.community.api.DecisionEvaluationService;
import org.camunda.community.api.soap.model.EvaluateDecisionRequest;
import org.camunda.community.api.soap.model.SoapDecisionVariables;
import org.camunda.community.api.soap.model.SoapVariableEntry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionEvaluationSoapEndpointTest {

    @Test
    void evaluateDecisionReturnsSuccessPayloadWhenServiceSucceeds() {

        DecisionEvaluationService service = Mockito.mock(DecisionEvaluationService.class);
        Mockito.when(service.evaluate(Mockito.any())).thenReturn(Map.of("decision", "approved"));

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(service, new ObjectMapper());

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        request.setDecisionDefinitionId("decision-id");
        SoapDecisionVariables variables = new SoapDecisionVariables();
        variables.setEntries(List.of(
                new SoapVariableEntry("team", "East Regional"),
                new SoapVariableEntry("state", "Alabama")
        ));
        request.setVariables(variables);

        var response = endpoint.evaluateDecision(request);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getResult().getAny().size());
        assertEquals("decision", response.getResult().getAny().getFirst().getTagName());
        assertEquals("approved", response.getResult().getAny().getFirst().getTextContent());
    }

    @Test
    void evaluateDecisionReturnsErrorPayloadWhenServiceFails() {

        DecisionEvaluationService service = Mockito.mock(DecisionEvaluationService.class);
        Mockito.when(service.evaluate(Mockito.any()))
                .thenThrow(new IllegalArgumentException("Either decisionDefinitionId or decisionDefinitionKey must be provided."));

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(service, new ObjectMapper());

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        var response = endpoint.evaluateDecision(request);

        assertFalse(response.isSuccess());
        assertEquals("Either decisionDefinitionId or decisionDefinitionKey must be provided.", response.getErrorMessage());
    }

    @Test
    void evaluateDecisionIgnoresNullAndBlankVariableEntries() {
        DecisionEvaluationService service = Mockito.mock(DecisionEvaluationService.class);
        Mockito.when(service.evaluate(Mockito.any())).thenReturn(Map.of("decision", "approved"));

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(service, new ObjectMapper());

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        request.setDecisionDefinitionId("decision-id");
        SoapDecisionVariables variables = new SoapDecisionVariables();
        variables.setEntries(Arrays.asList(
                null,
                new SoapVariableEntry("", "ignored"),
                new SoapVariableEntry("team", "East Regional")
        ));
        request.setVariables(variables);

        endpoint.evaluateDecision(request);

        ArgumentCaptor<DecisionDTO> captor = ArgumentCaptor.forClass(DecisionDTO.class);
        Mockito.verify(service).evaluate(captor.capture());
        var dto = captor.getValue();
        assertEquals(1, dto.getVariables().size());
        assertEquals("East Regional", dto.getVariables().get("team"));
    }

    @Test
    void evaluateDecisionHandlesNullVariableEntriesList() {
        DecisionEvaluationService service = Mockito.mock(DecisionEvaluationService.class);
        Mockito.when(service.evaluate(Mockito.any())).thenReturn(Map.of("decision", "approved"));

        DecisionEvaluationSoapEndpoint endpoint = new DecisionEvaluationSoapEndpoint(service, new ObjectMapper());

        EvaluateDecisionRequest request = new EvaluateDecisionRequest();
        request.setDecisionDefinitionId("decision-id");
        SoapDecisionVariables variables = new SoapDecisionVariables();
        variables.setEntries(null);
        request.setVariables(variables);

        var response = endpoint.evaluateDecision(request);

        assertTrue(response.isSuccess());
        ArgumentCaptor<DecisionDTO> captor = ArgumentCaptor.forClass(DecisionDTO.class);
        Mockito.verify(service).evaluate(captor.capture());
        assertNull(captor.getValue().getVariables());
    }
}