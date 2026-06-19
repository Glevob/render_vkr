package ru.accouting.student.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.accouting.student.model.User;
import ru.accouting.student.model.UserAuthority;
import ru.accouting.student.model.UserRole;
import ru.accouting.student.repository.UserRepository;
import ru.accouting.student.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@Order(1)
@RequiredArgsConstructor
public class UserDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createIfNotExists("asd", "asdasd", UserAuthority.FULL);
        createIfNotExists("asdasd", "asdasd", UserAuthority.TECHNOLOGIST);
    }

    private void createIfNotExists(String login, String rawPassword, UserAuthority authority) {
        if (userRepository.existsByLogin(login)) {
            return;
        }

        User user = new User()
                .setIdUser(null)
                .setLogin(login)
                .setPassword(passwordEncoder.encode(rawPassword));

        user = userRepository.save(user);

        UserRole role = new UserRole(null, authority, user);
        userRoleRepository.save(role);
    }
}