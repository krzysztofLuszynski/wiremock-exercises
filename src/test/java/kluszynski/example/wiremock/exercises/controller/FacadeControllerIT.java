package kluszynski.example.wiremock.exercises.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
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
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacadeControllerIT {
    private static final String SECOND_SERVICE_RESPONSE = "simpleString";

    private WireMockServer wireMockServer;

    @LocalServerPort
    private Integer port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(FacadeController.SECOND_SERVICE_PORT);
        this.wireMockServer.stubFor(get(urlEqualTo(FacadeController.SECOND_SERVICE_PATH))
                .willReturn(aResponse().withBody(SECOND_SERVICE_RESPONSE)));
        this.wireMockServer.start();
    }

    @Test
    void getTest() throws Exception {
        final ResponseEntity<String> getAllResponse = restTemplate.getForEntity(
                getServiceUrl("facade"), String.class);

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).isEqualTo(SECOND_SERVICE_RESPONSE);
    }

    private String getServiceUrl(final String endpointPath) throws MalformedURLException {
        return new URL("http://localhost:" + port + "/wiremock-exercises/" + endpointPath).toString();
    }
}
