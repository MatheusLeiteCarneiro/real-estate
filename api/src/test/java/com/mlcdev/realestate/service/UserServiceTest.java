package com.mlcdev.realestate.service;

import com.mlcdev.realestate.dto.UserCreateDTO;
import com.mlcdev.realestate.dto.UserDTO;
import com.mlcdev.realestate.dto.UserPatchDTO;
import com.mlcdev.realestate.entities.Role;
import com.mlcdev.realestate.entities.User;
import com.mlcdev.realestate.exception.BusinessRuleException;
import com.mlcdev.realestate.exception.ConflictException;
import com.mlcdev.realestate.repository.UserRepository;
import com.mlcdev.realestate.security.OAuth2AuthorizationCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {


    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2AuthorizationCleanupService oAuth2AuthorizationCleanupService;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("Create User")
    class CreateTests{

        @Test
        @DisplayName("Should create broker with encoded password and public fields")
        void createShouldSaveBrokerAndReturnPublicUserFields() {
            String username = "NewUsername";
            String rawPassword = "StrongPassword@123";
            String encodedPassword = "encoded_password";
            UUID generatedId = UUID.randomUUID();
            Instant createdAt = Instant.now();

            UserCreateDTO createDTO = UserCreateDTO.builder()
                    .username(username)
                    .password(rawPassword)
                    .build();

            when(userRepository.existsByUsername(username)).thenReturn(false);
            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

            when(userRepository.saveAndFlush(any(User.class)))
                    .thenAnswer(invocation -> {
                        User userToSave = invocation.getArgument(0);
                        userToSave.setId(generatedId);
                        userToSave.setCreatedAt(createdAt);
                        return userToSave;
                    });

            UserDTO result = userService.create(createDTO);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).saveAndFlush(userCaptor.capture());
            verify(passwordEncoder).encode(rawPassword);

            User savedUser = userCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(generatedId);
            assertThat(result.getUsername()).isEqualTo(username);

            assertThat(savedUser.getPassword())
                    .isEqualTo(encodedPassword)
                    .isNotEqualTo(rawPassword);

            assertThat(savedUser.getAuthorities())
                    .containsExactly(Role.ROLE_BROKER);

            assertThat(result.getAuthorities())
                    .containsExactly(Role.ROLE_BROKER.getAuthority());

            assertThat(result.getCreatedAt()).isEqualTo(createdAt);

            assertThat(result.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should reject duplicate username")
        void createShouldThrowConflictExceptionWhenUsernameAlreadyExists(){
            String username = "NewUsername";
            String rawPassword = "StrongPassword@123";

            UserCreateDTO createDTO = UserCreateDTO.builder()
                    .username(username)
                    .password(rawPassword)
                    .build();

            when(userRepository.existsByUsername(username)).thenReturn(true);

            assertThatThrownBy(() -> userService.create(createDTO))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Username not available");

        }
    }

    @Nested
    @DisplayName("Update user")
    class UpdateTests{

        @Test
        @DisplayName("Should update username")
        void updateShouldChangeUsernameWhenUsernameIsAvailable(){
            String newUsername = "newUsername";
            UserPatchDTO patchDTO = UserPatchDTO.builder().username(newUsername).build();
            UUID userId = UUID.randomUUID();
            User user = User.builder().id(userId).username("oldUsername").password("encoded_password").build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.existsByUsernameAndIdNot(newUsername, userId)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserDTO result = userService.update(patchDTO, userId);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).findById(userId);
            verify(userRepository).existsByUsernameAndIdNot(newUsername, userId);
            verify(userRepository).save(userCaptor.capture());
            verifyNoInteractions(passwordEncoder);

            User savedUser = userCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(newUsername);
            assertThat(savedUser.getUsername()).isEqualTo(newUsername);
            assertThat(savedUser.getPassword()).isEqualTo("encoded_password");
        }

        @Test
        @DisplayName("Should update password")
        void updateShouldEncodeAndChangePassword(){
            String username = "username";
            String newPassword = "NewStrongPassword@123";
            String newEncodedPassword = "new_encoded_password";
            UserPatchDTO patchDTO = UserPatchDTO.builder().password(newPassword).build();
            UUID userId = UUID.randomUUID();
            User user = User.builder().id(userId).username(username).password("old_password").build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(passwordEncoder.encode(newPassword)).thenReturn(newEncodedPassword);

            UserDTO result = userService.update(patchDTO, userId);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).findById(userId);
            verify(passwordEncoder).encode(newPassword);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(username);
            assertThat(savedUser.getPassword()).isEqualTo(newEncodedPassword);
        }

            @Test
            @DisplayName("Should reject username used by another user")
            void updateShouldThrowConflictExceptionWhenUsernameBelongsToAnotherUser(){
                String newUsername = "newUsername";
                UserPatchDTO patchDTO = UserPatchDTO.builder().username(newUsername).build();
                UUID userId = UUID.randomUUID();
                User user = User.builder().id(userId).username("oldUsername").password("encoded_password").build();
                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(userRepository.existsByUsernameAndIdNot(newUsername, userId)).thenReturn(true);

                assertThatThrownBy(() -> userService.update(patchDTO, userId)).isInstanceOf(ConflictException.class).hasMessage("Username not available");

                verify(userRepository).findById(userId);
                verify(userRepository).existsByUsernameAndIdNot(newUsername, userId);
                verify(userRepository, never()).save(any(User.class));
                verifyNoInteractions(passwordEncoder);

            }


    }

    @Nested
    @DisplayName("Toggle user status")
    class ToggleActiveTests{

        @Test
        @DisplayName("Should prevent admin deactivation")
        void toggleActiveShouldThrowBusinessRuleExceptionWhenUserIsAdmin(){
            UUID generatedId = UUID.randomUUID();
            User user = User.builder()
                    .id(generatedId)
                    .username("username")
                    .password("encoded-password")
                    .authorities(Set.of(Role.ROLE_BROKER, Role.ROLE_ADMIN))
                    .active(true)
                    .build();
            when(userRepository.findById(generatedId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.toggleActive(generatedId))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Admin user cannot be deactivated");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should invalidate tokens when broker is deactivated")
        void toggleActiveShouldInvalidateAuthorizationsWhenBrokerIsDeactivated(){
            UUID generatedId = UUID.randomUUID();
            String username = "username";
            User user = User.builder()
                    .id(generatedId)
                    .username(username)
                    .password("encoded-password")
                    .authorities(Set.of(Role.ROLE_BROKER))
                    .active(true)
                    .build();

            when(userRepository.findById(generatedId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(oAuth2AuthorizationCleanupService).invalidateUserTokens(username);


            UserDTO result = userService.toggleActive(generatedId);

            verify(userRepository).findById(generatedId);
            verify(userRepository).save(any(User.class));
            verify(oAuth2AuthorizationCleanupService).invalidateUserTokens(username);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(generatedId);
            assertThat(result.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should keep tokens when broker is activated")
        void toggleActiveShouldNotInvalidateAuthorizationsWhenBrokerIsActivated(){
            UUID generatedId = UUID.randomUUID();
            String username = "username";
            User user = User.builder()
                    .id(generatedId)
                    .username(username)
                    .password("encoded-password")
                    .authorities(Set.of(Role.ROLE_BROKER))
                    .active(false)
                    .build();

            when(userRepository.findById(generatedId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


            UserDTO result = userService.toggleActive(generatedId);

            verify(userRepository).findById(generatedId);
            verify(userRepository).save(any(User.class));
            verify(oAuth2AuthorizationCleanupService, never()).invalidateUserTokens(username);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(generatedId);
            assertThat(result.isActive()).isTrue();
        }

    }

}
