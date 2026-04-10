package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.CreateUserRequest;
import com.gvp.financialdashboard.domain.dto.UserResponse;

public interface UserService {
    UserResponse create(CreateUserRequest createUserRequest);
}
