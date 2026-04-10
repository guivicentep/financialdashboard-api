package com.gvp.financialdashboard.service.impl;

import com.gvp.financialdashboard.domain.dto.CreateUserRequest;
import com.gvp.financialdashboard.domain.dto.UserResponse;
import com.gvp.financialdashboard.domain.entity.User;
import com.gvp.financialdashboard.exception.BusinessException;
import com.gvp.financialdashboard.repository.UserRepository;
import com.gvp.financialdashboard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByClerkId(request.clerkId())) {
            throw new BusinessException("Usuário já cadastrado");
        }

        var user = User.builder()
                .clerkId(request.clerkId())
                .build();

        return UserResponse.from(userRepository.save(user));
    }
}
