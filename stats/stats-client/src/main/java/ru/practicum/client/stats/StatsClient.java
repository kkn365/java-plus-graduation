package ru.practicum.client.stats;


import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.client.BaseClient;
import ru.practicum.dto.HitsStatDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class StatsClient extends BaseClient {

    public Optional<Collection<HitsStatDTO>> getAll(LocalDateTime start,
                                                    LocalDateTime end,
                                                    List<String> uris,
                                                    Boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/stats");
        Map<String, Object> params = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (!Objects.isNull(start)) {
            builder.queryParam("start", "{start}");
            params.put("start", start.format(formatter));
        }

        if (!Objects.isNull(end)) {
            builder.queryParam("end", "{end}");
            params.put("end", end.format(formatter));
        }

        if (!Objects.isNull(uris) && !uris.isEmpty()) {
            builder.queryParam("uris", "{uris}");
            params.put("uris", String.join(" ", uris));
        }

        if (!Objects.isNull(unique)) {
            builder.queryParam("unique", "{unique}");
            params.put("unique", unique);
        }

        String uri = builder.build().toUriString();

        try {
            return get(uri, params, new TypeReference<Collection<HitsStatDTO>>() {
            });
        } catch (Exception e) {
            log.info("StatsClient::getAll error: {}", e.getMessage());
        }

        return Optional.empty();
    }

}