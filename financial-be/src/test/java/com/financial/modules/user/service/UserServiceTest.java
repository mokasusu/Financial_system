package com.financial.modules.user.service;

import com.financial.common.exception.EmailAlreadyExistsException;
import com.financial.common.exception.UserNotFoundException;
import com.financial.common.response.PageResponse;
import com.financial.modules.user.dto.UserRequest;
import com.financial.modules.user.dto.UserResponse;
import com.financial.modules.user.dto.UserStatusUpdateRequest;
import com.financial.modules.user.dto.UserUpdateRequest;
import com.financial.modules.user.entity.User;
import com.financial.modules.user.enums.AuthProvider;
import com.financial.modules.user.enums.UserStatus;
import com.financial.modules.user.repository.UserRepository;
import com.financial.modules.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UserRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("$2a$10$hashedPassword")
                .phoneNumber("+1234567890")
                .avatarUrl("https://example.com/avatar.png")
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = UserRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("securePassword123")
                .phoneNumber("+1234567890")
                .avatarUrl("https://example.com/avatar.png")
                .build();
    }

    @Test
    @DisplayName("createUser should save and return UserResponse with encrypted password and ACTIVE status")
    void createUser_Success() {
        when(userRepository.existsByEmail(sampleRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(sampleRequest.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.createUser(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.getProvider()).isEqualTo(AuthProvider.LOCAL);
        verify(passwordEncoder).encode("securePassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser should throw EmailAlreadyExistsException when email exists")
    void createUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(sampleRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(sampleRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getUserById should return UserResponse when user exists")
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("getUserById should throw UserNotFoundException when user does not exist")
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    @DisplayName("getAllUsers should return paginated PageResponse")
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(sampleUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        PageResponse<UserResponse> response = userService.getAllUsers(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("updateUser should update user fields successfully")
    void updateUser_Success() {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .fullName("John Updated")
                .phoneNumber("+9876543210")
                .avatarUrl("https://example.com/new.png")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.updateUser(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(userRepository).save(sampleUser);
        assertThat(sampleUser.getFullName()).isEqualTo("John Updated");
        assertThat(sampleUser.getPhoneNumber()).isEqualTo("+9876543210");
    }

    @Test
    @DisplayName("updateUserStatus should update status successfully")
    void updateUserStatus_Success() {
        UserStatusUpdateRequest statusRequest = UserStatusUpdateRequest.builder()
                .status(UserStatus.LOCKED)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.updateUserStatus(1L, statusRequest);

        assertThat(response).isNotNull();
        assertThat(sampleUser.getStatus()).isEqualTo(UserStatus.LOCKED);
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("deactivateUser should set status to INACTIVE")
    void deactivateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.deactivateUser(1L);

        assertThat(sampleUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(sampleUser);
    }
}

