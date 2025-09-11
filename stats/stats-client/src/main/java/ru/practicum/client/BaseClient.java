package ru.practicum.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.client.exception.BadRequestException;
import ru.practicum.client.exception.InternalServerException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
@Setter
@Slf4j
public class BaseClient {
    @Value("${stats-client.server-url:http://localhost:9090}")
    private String serverDiscoveryUrlName;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DiscoveryClient discoveryClient;

    public <T> Optional<T> get(String path, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return doRequest(HttpMethod.GET, path, null, null, typeReference);
    }

    public <T> Optional<T> get(String path, Map<String, ?> params, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return doRequest(HttpMethod.GET, path, params, null, typeReference);
    }

    public <K, T> Optional<T> post(String path, K body) throws IOException, InterruptedException {
        return doRequest(HttpMethod.POST, path, null, body, null);
    }

    public <K, T> Optional<T> post(String path, K body, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return doRequest(HttpMethod.POST, path, null, body, typeReference);
    }

    public <K, T> Optional<T> post(String path, Map<String, ?> params, K body, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return doRequest(HttpMethod.POST, path, params, body, typeReference);
    }

    private <T, K> Optional<T> doRequest(HttpMethod method, String path, Map<String, ?> params, K body, TypeReference<T> typeReference) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();



        URI uri = UriComponentsBuilder
                .fromUriString(getServerUrl() + path)
                .buildAndExpand(Objects.isNull(params) ? Map.of() : params)
                .encode()
                .toUri();


        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json;charset=UTF-8");


        HttpRequest.BodyPublisher publisher;
        if (List.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.DELETE).contains(method) || Objects.isNull(body)) {
            publisher = HttpRequest.BodyPublishers.noBody();
        } else {
            String jsonBody = objectMapper.writeValueAsString(body);
            publisher = HttpRequest.BodyPublishers.ofString(jsonBody);
        }

        requestBuilder.method(method.name(), publisher);
        log.info("Build HTTP request. Method: {}, uri: {}, ", method, uri.toASCIIString());
        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300 && !response.body().isEmpty()) {
            T result = objectMapper.readValue(response.body(), typeReference);
            return Optional.of(result);
        } else if (response.statusCode() >= 300 && response.statusCode() < 400) {
            throw new RuntimeException(response.body());
        } else if (response.statusCode() >= 400 && response.statusCode() < 500) {
            throw new BadRequestException(response.body());
        } else if (response.statusCode() >= 500) {
            throw new InternalServerException(response.body());
        }

        return Optional.empty();
    }

    public String getServerUrl() {
        try {
            ServiceInstance serviceInstance = discoveryClient
                    .getInstances(serverDiscoveryUrlName)
                    .getFirst();
            String serverUrl = serviceInstance.getUri().toString();
            log.info("Get {} uri: {}", serverUrl, serverUrl);
            return serverUrl;
        } catch (Exception exception) {
            throw new InternalServerException(
                    "Error finding statistics service address with id: " + serverDiscoveryUrlName + ". Error: " + exception
            );
        }


    }
}