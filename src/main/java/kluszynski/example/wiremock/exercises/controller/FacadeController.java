package kluszynski.example.wiremock.exercises.controller;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RestController
public class FacadeController {
    static final int SECOND_SERVICE_PORT = 8082;
    static final String SECOND_SERVICE_BASE_URL = "http://localhost:" + SECOND_SERVICE_PORT;
    static final String SECOND_SERVICE_PATH = "/secondService";

    @GetMapping("/facade")
    public String get() {
        RetryTemplate template = new RetryTemplate();

        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(3);

        template.setRetryPolicy(policy);

        return template.execute(context -> {
            Mono<String> otherServiceResponse = callOtherService();
            return otherServiceResponse.block();
        });
    }

    private Mono<String> callOtherService() {
        WebClient webClient = createWebClient();

        return webClient.get()
                .uri(SECOND_SERVICE_PATH)
                .retrieve()
                .bodyToMono(String.class);
    }

    private WebClient createWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .responseTimeout(Duration.ofMillis(1000))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(1000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(1000, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(SECOND_SERVICE_BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
