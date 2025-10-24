package io.github.lcmdev.desafio.payment.client;

import static java.util.Objects.nonNull;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthorizerClient {

  private final WebClient webClient;

  public AuthorizerClient(@Value("${external.authorizer.payment.url}") String baseUrl) {
    this.webClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build();
  }

  public boolean authorize() {
    try {
      String response = webClient.get()
          .retrieve()
          .bodyToMono(String.class)
          .timeout(Duration.ofSeconds(5))
          .block();
      return nonNull(response) && (response.toLowerCase().contains("authorized")
          || response.toLowerCase().contains("approved"));
    } catch (Exception e) {
      return false;
    }
  }
}