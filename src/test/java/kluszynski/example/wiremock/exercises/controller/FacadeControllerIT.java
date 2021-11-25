package kluszynski.example.wiremock.exercises.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacadeControllerIT {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAllPersonsEmptyDatabase() throws Exception {
        final ResponseEntity<String> getAllResponse = restTemplate.getForEntity(
                getServiceUrl("facade"), String.class);

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).isEqualTo("Hello world");
    }

    private String getServiceUrl(final String endpointPath) throws MalformedURLException {
        return new URL("http://localhost:" + port + "/wiremock-exercises/" + endpointPath).toString();
    }
}
