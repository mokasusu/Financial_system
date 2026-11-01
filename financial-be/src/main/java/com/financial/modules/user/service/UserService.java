package com.financial.modules.user.service;

import com.financial.common.response.PageResponse;
import com.financial.modules.user.dto.UserRequest;
import com.financial.modules.user.dto.UserResponse;
import com.financial.modules.user.dto.UserStatusUpdateRequest;
import com.financial.modules.user.dto.UserUpdateRequest;
import com.financial.modules.user.enums.UserStatus;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(UserRequest request);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    UserResponse updateUser(Long id, UserRequest request);

    UserResponse updateUserStatus(Long id, UserStatusUpdateRequest request);

    UserResponse updateUserStatus(Long id, UserStatus status);

    void deactivateUser(Long id);
}

