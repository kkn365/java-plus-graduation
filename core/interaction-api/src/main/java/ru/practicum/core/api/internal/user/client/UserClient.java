package ru.practicum.core.api.internal.user.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.api.internal.user.dto.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service", path = "/internal/user", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/{userId}")
    ResponseEntity<UserShortDto> getUser(@PathVariable Long userId) throws FeignException;

    @GetMapping
    ResponseEntity<List<UserShortDto>> getUsers(@RequestParam List<Long> ids) throws FeignException;
}
