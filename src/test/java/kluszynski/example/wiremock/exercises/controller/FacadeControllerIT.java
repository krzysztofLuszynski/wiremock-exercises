package kluszynski.example.wiremock.exercises.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = FacadeController.SECOND_SERVICE_PORT)
class FacadeControllerIT {
    private static final String SECOND_SERVICE_RESPONSE = "simpleString";

    @LocalServerPort
    private Integer port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getNoTimeout() throws Exception {
        stubFor(get("/secondService")
                .willReturn(aResponse()
                        .withBody(SECOND_SERVICE_RESPONSE)));

        final ResponseEntity<String> getAllResponse = restTemplate.getForEntity(
                getServiceUrl("facade"), String.class);

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).isEqualTo(SECOND_SERVICE_RESPONSE);
    }

    @Test
    void getTimeout() throws Exception {
        stubFor(get("/secondService")
                .willReturn(aResponse()
                        .withBody(SECOND_SERVICE_RESPONSE)
                        .withFixedDelay(2000)));

        final ResponseEntity<String> getAllResponse = restTemplate.getForEntity(
                getServiceUrl("facade"), String.class);

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getTwoTimeoutsAndLastRequestnNotTimeout() throws Exception {
        stubFor(get("/secondService").inScenario("Two timeouts")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withBody(SECOND_SERVICE_RESPONSE)
                        .withFixedDelay(2000))
                .willSetStateTo("Second timeout"));

        stubFor(get("/secondService").inScenario("Two timeouts")
                .whenScenarioStateIs("Second timeout")
                .willReturn(aResponse()
                        .withBody(SECOND_SERVICE_RESPONSE)
                        .withFixedDelay(2000))
                .willSetStateTo("Third call no timeout"));

        stubFor(get("/secondService").inScenario("Two timeouts")
                .whenScenarioStateIs("Third call no timeout")
                .willReturn(aResponse()
                        .withBody(SECOND_SERVICE_RESPONSE)));

        final ResponseEntity<String> getAllResponse = restTemplate.getForEntity(
                getServiceUrl("facade"), String.class);

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).isEqualTo(SECOND_SERVICE_RESPONSE);
    }

    private String getServiceUrl(final String endpointPath) throws MalformedURLException {
        return new URL("http://localhost:" + port + "/wiremock-exercises/" + endpointPath).toString();
    }
}
