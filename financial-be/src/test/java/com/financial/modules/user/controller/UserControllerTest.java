package com.financial.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.exception.GlobalExceptionHandler;
import com.financial.common.exception.UserNotFoundException;
import com.financial.common.response.PageResponse;
import com.financial.modules.user.dto.UserRequest;
import com.financial.modules.user.dto.UserResponse;
import com.financial.modules.user.dto.UserStatusUpdateRequest;
import com.financial.modules.user.dto.UserUpdateRequest;
import com.financial.modules.user.enums.AuthProvider;
import com.financial.modules.user.enums.UserStatus;
import com.financial.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = UserResponse.builder()
                .id(1L)
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .phoneNumber("+1234567890")
                .avatarUrl("https://example.com/avatar.png")
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/users should create user and return 201 Created")
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        UserRequest request = UserRequest.builder()
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .avatarUrl("https://example.com/avatar.png")
                .build();

        when(userService.createUser(any(UserRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/users with invalid input should return 400 Bad Request")
    void createUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserRequest request = UserRequest.builder()
                .fullName("")
                .email("invalid-email")
                .password("123")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("GET /api/users should return 200 OK with paginated results")
    void getAllUsers_ReturnsOk() throws Exception {
        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .items(List.of(sampleResponse))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isFirst(true)
                .isLast(true)
                .build();

        when(userService.getAllUsers(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/users?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].email").value("jane.doe@example.com"));
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 200 OK when user exists")
    void getUserById_Success_ReturnsOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.fullName").value("Jane Doe"));
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 404 Not Found when user does not exist")
    void getUserById_NotFound_ReturnsNotFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} should update user and return 200 OK")
    void updateUser_Success_ReturnsOk() throws Exception {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .fullName("Jane Updated")
                .phoneNumber("+999999999")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/status should update status and return 200 OK")
    void updateUserStatus_Success_ReturnsOk() throws Exception {
        UserStatusUpdateRequest statusRequest = UserStatusUpdateRequest.builder()
                .status(UserStatus.LOCKED)
                .build();

        sampleResponse.setStatus(UserStatus.LOCKED);
        when(userService.updateUserStatus(eq(1L), any(UserStatusUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("LOCKED"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} should deactivate user and return 200 OK")
    void deactivateUser_Success_ReturnsOk() throws Exception {
        doNothing().when(userService).deactivateUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deactivated successfully"));
    }
}
