package ru.accouting.student.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.accouting.student.model.User;
import ru.accouting.student.model.UserAuthority;
import ru.accouting.student.model.UserRole;
import ru.accouting.student.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Динамическая фильтрация списка пользователей с поддержкой постраничного вывода (пагинации)
    public Page<User> getAllUsers(int page, int size, String searchQuery, String authorityRaw) {
        // Инициализация настроек пагинации
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        // Проверка: были ли переданы параметры фильтрации
        // Строки не дожы быть пустыми
        boolean hasSearch = searchQuery != null && !searchQuery.isBlank();
        boolean hasAuthority = authorityRaw != null && !authorityRaw.isBlank();

        // Очищаем поисковый запрос от случайных начальных и конечных пробелов
        String cleanSearch = hasSearch ? searchQuery.trim() : "";
        UserAuthority authority = null;

        // Безопасный парсинг строкового значение роли в строго типизированный Enum
        if (hasAuthority) {
            try {
                authority = UserAuthority.valueOf(authorityRaw);
            } catch (IllegalArgumentException e) {
                // Если передана несуществующая роль, мягко игнорируем этот фильтр, не роняя приложение
                hasAuthority = false;
            }
        }

        // Определение сценариев фильтрации в зависимости от комбинации заполненных полей

        // 1. Выбраны логин и роль
        if (hasSearch && hasAuthority) {
            return userRepository.findByLoginContainingIgnoreCaseAndUserRoles_UserAuthority(cleanSearch, authority, pageable);
        }
        // 2. Выбран только логин
        if (hasSearch) {
            return userRepository.findByLoginContainingIgnoreCase(cleanSearch, pageable);
        }
        // 3. Выбрана только роль
        if (hasAuthority) {
            return userRepository.findByUserRoles_UserAuthority(authority, pageable);
        }
        // 4. Фильтры не заданы — отдаем всех
        return userRepository.findAll(pageable);
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));
    }

    @Transactional
    public void updateUser(Long id,
                           String login,
                           String rawPassword,
                           UserAuthority authority) {

        // Получение пользователя из базы данных (выбросит ошибку, если ID некорректен)
        User user = getUser(id);

        // Обновление логина текстовым значением
        user.setLogin(login);

        // Если передан новый пароль (не null и не пустая строка), хэшируем и сохраняем его
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        // Очистка старой роли пользователя для последующей перезаписи
        user.getUserRoles().clear();

        // Если указана новая роль, формируем новый объект связи UserRole
        if (authority != null) {
            UserRole role = new UserRole();
            role.setUserAuthority(authority); // Устанавливаем уровень прав (Enum)
            role.setUser(user);               // Настраиваем обратную связь на пользователя (внешний ключ)
            user.getUserRoles().add(role);    // Добавляем созданную роль в коллекцию пользователя
        }
    }

    @Transactional
    public void createUser(String login, String rawPassword, UserAuthority authority) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(rawPassword));

        UserRole role = new UserRole();
        role.setUserAuthority(authority);
        role.setUser(user);

        user.setUserRoles(new ArrayList<>(List.of(role)));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUser(id);
        userRepository.delete(user);
    }
}