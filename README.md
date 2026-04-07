# Orchestration API Client Java

**Last Updated:** April 2026

A Spring Boot application that provides **REST and SOAP API endpoints** for interacting with **Camunda 8 SaaS Orchestration Cluster** using the official [Camunda Java Client](https://docs.camunda.io/docs/apis-tools/java-client/). It exposes endpoints for topology retrieval, decision definition lookup, decision definition search, and DMN evaluation.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
  - [Environment Variables](#environment-variables)
  - [Network Access Requirements](#network-access-requirements)
  - [Setting Environment Variables](#setting-environment-variables)
  - [Using direnv (macOS / Linux)](#using-direnv-macos--linux)
- [Running the Application](#running-the-application)
- [Swagger UI / OpenAPI Docs](#swagger-ui--openapi-docs)
- [REST API Endpoints](#rest-api-endpoints)
- [SOAP Endpoint](#soap-endpoint)
- [Request & Response Models](#request--response-models)
- [Running Tests](#running-tests)
- [Building a JAR](#building-a-jar)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

This application acts as a **Java-based REST and SOAP proxy** for Camunda 8 Orchestration APIs. It is useful for:

- Retrieving cluster topology and decision definitions.
- Searching deployed DMN decision definitions with paging/filter criteria.
- Evaluating decision definitions (DMN) with input variables.
- Exposing SOAP/WSDL interfaces for enterprise integrations.
- Serving as a reference implementation of `camunda-client-java` in Spring Boot.

---

## Tech Stack

| Technology | Version |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.0.5 |
| Spring Web MVC | via `spring-boot-starter-web` |
| Spring Web Services (SOAP) | via `spring-boot-starter-web-services` |
| Bean Validation | via `spring-boot-starter-validation` |
| Camunda Java Client | 8.8.21 |
| OpenAPI + Swagger UI | `springdoc-openapi-starter-webmvc-ui:3.0.2` |
| JAXB | `jakarta.xml.bind-api`, `jaxb-runtime` |
| WSDL4J | 1.6.3 |
| Maven | 3.x (via wrapper `mvnw`) |

---

## Project Structure

```text
src/
├── main/
│   ├── java/org/camunda/community/api/
│   │   ├── OrchestrationApiClientApplication.java
│   │   ├── CamundaClientConfiguration.java
│   │   ├── DecisionEvaluationService.java
│   │   ├── DecisionDTO.java
│   │   ├── OpenApiConfig.java
│   │   ├── rest/
│   │   │   └── DecisionDefinitionController.java
│   │   └── soap/
│   │       ├── SoapWebServiceConfig.java
│   │       ├── DecisionEvaluationSoapEndpoint.java
│   │       └── model/
│   │           ├── EvaluateDecisionRequest.java
│   │           ├── EvaluateDecisionResponse.java
│   │           ├── SoapDecisionVariables.java
│   │           └── SoapVariableEntry.java
│   └── resources/
│       ├── application.yaml
│       └── decision-evaluation.xsd
└── test/
    └── java/org/camunda/community/api/
        ├── OrchestrationApiClientApplicationTests.java
        ├── rest/DecisionDefinitionControllerTest.java
        └── soap/
            ├── DecisionEvaluationSoapEndpointTest.java
            └── DecisionEvaluationSoapEndpointContractTest.java
```

---

## Prerequisites

- Java 21+
- Maven 3.x (or use `./mvnw`)
- Camunda 8 SaaS cluster credentials:
  - Cluster ID
  - Cluster Region
  - OAuth Client ID
  - OAuth Client Secret

---

## Configuration

### Environment Variables

Set the following variables before starting the app:

| Environment Variable | Description |
| --- | --- |
| `CAMUNDA_CLUSTER_ID` | Camunda SaaS cluster UUID |
| `CAMUNDA_CLUSTER_REGION` | Cluster region (for example `cle-1`) |
| `CAMUNDA_CLIENT_ID` | OAuth M2M client ID |
| `CAMUNDA_CLIENT_SECRET` | OAuth M2M client secret |

These map to `src/main/resources/application.yaml`:

```yaml
camunda:
  cluster:
    id: ${CAMUNDA_CLUSTER_ID}
    region: ${CAMUNDA_CLUSTER_REGION}
  client:
    id: ${CAMUNDA_CLIENT_ID}
    secret: ${CAMUNDA_CLIENT_SECRET}
```

### Network Access Requirements

Allow outbound access to:

- Zeebe endpoint: `{CAMUNDA_CLUSTER_ID}.{CAMUNDA_CLUSTER_REGION}.zeebe.camunda.io:443`
- OAuth endpoint: `https://login.cloud.camunda.io/oauth/token`

### Setting Environment Variables

#### macOS / Linux (zsh)

```bash
export CAMUNDA_CLUSTER_ID=your-cluster-id
export CAMUNDA_CLUSTER_REGION=your-cluster-region
export CAMUNDA_CLIENT_ID=your-client-id
export CAMUNDA_CLIENT_SECRET=your-client-secret
```

#### Windows (PowerShell)

```powershell
$env:CAMUNDA_CLUSTER_ID = "your-cluster-id"
$env:CAMUNDA_CLUSTER_REGION = "your-cluster-region"
$env:CAMUNDA_CLIENT_ID = "your-client-id"
$env:CAMUNDA_CLIENT_SECRET = "your-client-secret"
```

### Using direnv (macOS / Linux)

```bash
brew install direnv
echo 'eval "$(direnv hook zsh)"' >> ~/.zshrc
```

Create `.envrc` in the project root with your values, then allow it:

```bash
direnv allow .
```

---

## Running the Application

```bash
./mvnw clean spring-boot:run
```

The app runs on `http://localhost:8080`.

---

## Swagger UI / OpenAPI Docs

When the app is running:

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`
- `http://localhost:8080/v3/api-docs.yaml`

---

## REST API Endpoints

Base path: `/api/camunda`

- `GET /topology`
- `GET /decision-definitions/{decisionDefinitionKey}`
- `GET /decision-definitions/{decisionDefinitionKey}/xml`
- `POST /decision-definitions/search`
- `POST /decision-definitions/evaluation`

### Search Decision Definitions (Example)

```json
{
  "page": {
    "from": 0,
    "limit": 100
  },
  "sort": [
    {
      "field": "decisionDefinitionKey",
      "order": "ASC"
    }
  ],
  "filter": {
    "decisionDefinitionId": "new-hire-onboarding-workflow",
    "name": "string",
    "decisionDefinitionKey": "2251799813326547"
  }
}
```

### Evaluate Decision (Examples)

By ID:

```json
{
  "decisionDefinitionId": "my-decision-id",
  "variables": {
    "amount": 100
  }
}
```

By Key:

```json
{
  "decisionDefinitionKey": "2251799813326547",
  "variables": {
    "country": "US"
  }
}
```

---

## SOAP Endpoint

SOAP endpoint path: `/ws/*`

### WSDL Access

- `http://localhost:8080/ws/decisionEvaluation.wsdl`

### SOAP Request Example

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:dec="http://camunda.org/consulting/decision-evaluation">
  <soapenv:Header/>
  <soapenv:Body>
    <dec:evaluateDecisionRequest>
      <dec:decisionDefinitionId>myDecisionId</dec:decisionDefinitionId>
      <dec:variables>
        <dec:entry>
          <dec:key>team</dec:key>
          <dec:value>East Regional</dec:value>
        </dec:entry>
      </dec:variables>
    </dec:evaluateDecisionRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

### SOAP Response Example

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
  <SOAP-ENV:Body>
    <ns2:evaluateDecisionResponse xmlns:ns2="http://camunda.org/consulting/decision-evaluation">
      <ns2:success>true</ns2:success>
      <ns2:result>{"outcome":"approved"}</ns2:result>
    </ns2:evaluateDecisionResponse>
  </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

## Request & Response Models

### `DecisionDTO`

```json
{
  "decisionDefinitionId": "string",
  "decisionDefinitionKey": "string",
  "variables": {
    "firstInputKey": "firstInputValue",
    "secondInputKey": "secondInputValue"
  }
}
```

`variables` is represented as `Map<String, Object>`.

### SOAP Models

`decision-evaluation.xsd` defines:

- `evaluateDecisionRequest`
- `evaluateDecisionResponse`
- `SoapDecisionVariables` with repeated `entry`
- `SoapVariableEntry` (`key` + `value`, where `value` is `xsd:anyType`)

---

## Running Tests

Run all tests:

```bash
./mvnw test
```

Run specific tests:

```bash
./mvnw test -Dtest=DecisionDefinitionControllerTest
./mvnw test -Dtest=DecisionEvaluationSoapEndpointTest
./mvnw test -Dtest=DecisionEvaluationSoapEndpointContractTest
./mvnw test -Dtest=OrchestrationApiClientApplicationTests
```

---

## Building a JAR

```bash
./mvnw clean package
```

Artifact:

```text
target/orchestration-api-client-java-0.0.1-SNAPSHOT.jar
```

Run it:

```bash
java -jar target/orchestration-api-client-java-0.0.1-SNAPSHOT.jar
```

---

## Troubleshooting

### Missing environment variables

If startup fails with unresolved placeholders, make sure all four Camunda environment variables are set.

### Swagger UI cannot load OpenAPI docs

- Confirm app is running on port `8080`.
- Verify `http://localhost:8080/v3/api-docs` returns JSON.

### WSDL not accessible

- Confirm app is running.
- Verify `http://localhost:8080/ws/decisionEvaluation.wsdl`.
- Check logs for SOAP configuration issues.

---

## Contributing

1. Fork the repository.
2. Create a branch: `git checkout -b feature/my-feature`
3. Commit: `git commit -m 'Add my feature'`
4. Push: `git push origin feature/my-feature`
5. Open a pull request.

---

## License

This repository currently does not include a `LICENSE` file. Add one (for example Apache 2.0) before external distribution.

