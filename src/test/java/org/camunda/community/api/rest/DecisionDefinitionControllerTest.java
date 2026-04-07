package org.camunda.community.api.rest;

import org.camunda.community.api.DecisionEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DecisionDefinitionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DecisionEvaluationService decisionEvaluationService;

    @InjectMocks
    private DecisionDefinitionController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void topologyReturnsOkPayload() throws Exception {
        given(decisionEvaluationService.topology()).willReturn(Map.of("clusterSize", 3));

        mockMvc.perform(get("/api/camunda/topology"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"clusterSize\":3}"));
    }

    @Test
    void getDecisionDefinitionReturnsInternalServerErrorWhenServiceFails() throws Exception {
        given(decisionEvaluationService.getDecisionDefinition(42L))
                .willThrow(new RuntimeException("camunda unavailable"));

        mockMvc.perform(get("/api/camunda/decision-definitions/42"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error getting decision definition: camunda unavailable"));
    }

    @Test
    void searchDecisionDefinitionsForwardsJsonBody() throws Exception {
        given(decisionEvaluationService.searchDecisionDefinitions(any())).willReturn(Map.of("count", 1));

        mockMvc.perform(post("/api/camunda/decision-definitions/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filter\":{\"name\":\"loan\"}}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"count\":1}"));

        verify(decisionEvaluationService).searchDecisionDefinitions(any());
    }

    @Test
    void evaluateDecisionDefinitionReturnsBadRequestOnValidationError() throws Exception {
        given(decisionEvaluationService.evaluate(any()))
                .willThrow(new IllegalArgumentException("Either decisionDefinitionId or decisionDefinitionKey must be provided."));

        mockMvc.perform(post("/api/camunda/decision-definitions/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variables\":{}}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Either decisionDefinitionId or decisionDefinitionKey must be provided."));
    }

    @Test
    void evaluateDecisionDefinitionReturnsOkOnSuccess() throws Exception {
        given(decisionEvaluationService.evaluate(any())).willReturn(Map.of("result", "approved"));

        mockMvc.perform(post("/api/camunda/decision-definitions/evaluation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decisionDefinitionId": "decision-1",
                                  "variables": {
                                    "amount": 100
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"result\":\"approved\"}"));

        verify(decisionEvaluationService).evaluate(any());
    }

    @Test
    void getDecisionDefinitionXmlUsesPathVariable() throws Exception {
        given(decisionEvaluationService.getDecisionDefinitionXml(99L)).willReturn(Map.of("xml", "<dmn />"));

        mockMvc.perform(get("/api/camunda/decision-definitions/99/xml"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"xml\":\"<dmn />\"}"));

        verify(decisionEvaluationService).getDecisionDefinitionXml(eq(99L));
    }
}

