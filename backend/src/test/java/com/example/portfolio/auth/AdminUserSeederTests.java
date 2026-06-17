package com.example.portfolio.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.portfolio.user.User;
import com.example.portfolio.user.UserRepository;
import com.example.portfolio.user.UserRole;
import com.example.portfolio.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminUserSeederTests {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UserRepository userRepository;

    @Test
    void skipsWhenSeedIsDisabled() {
        AdminUserSeeder seeder = new AdminUserSeeder(
                new AdminSeedProperties(false, "admin@example.com", "secret"),
                passwordEncoder,
                userRepository);

        seeder.run(null);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createsActiveAdminWhenEnabledAndMissing() {
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("admin@example.com")).thenReturn(false);
        AdminUserSeeder seeder = new AdminUserSeeder(
                new AdminSeedProperties(true, "admin@example.com", "secret"),
                passwordEncoder,
                userRepository);

        seeder.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("admin@example.com");
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getPasswordHash()).isNotEqualTo("secret");
        assertThat(passwordEncoder.matches("secret", saved.getPasswordHash())).isTrue();
    }

    @Test
    void skipsWhenAdminAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull("admin@example.com")).thenReturn(true);
        AdminUserSeeder seeder = new AdminUserSeeder(
                new AdminSeedProperties(true, "admin@example.com", "secret"),
                passwordEncoder,
                userRepository);

        seeder.run(null);

        verify(userRepository, never()).save(any(User.class));
    }
}
