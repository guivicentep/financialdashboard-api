package com.gvp.financialdashboard.controller;

import com.gvp.financialdashboard.domain.dto.CreateUserRequest;
import com.gvp.financialdashboard.domain.dto.UserResponse;
import com.gvp.financialdashboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        var response = userService.create(request);
        return ResponseEntity.created(URI.create("/users/" + response.id())).body(response);
    }
}
