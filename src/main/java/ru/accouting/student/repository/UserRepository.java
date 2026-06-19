package ru.accouting.student.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.accouting.student.model.User;
import ru.accouting.student.model.UserAuthority;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Поиск по Логину
    Optional<User> findByLogin(String login);

    // Постраничный поиск пользователей по частичному совпадению логина без учета регистра
    Page<User> findByLoginContainingIgnoreCase(String login, Pageable pageable);

    // Поиск всех пользователей
    List<User> findAll();

    boolean existsByLogin(String login);

    // Постраничный поиск пользователей по частичному логину и фильтр по роли
    Page<User> findByLoginContainingIgnoreCaseAndUserRoles_UserAuthority(String login, UserAuthority authority, Pageable pageable);

    // Постраничный поиск пользователей по роли
    Page<User> findByUserRoles_UserAuthority(UserAuthority authority, Pageable pageable);
}
