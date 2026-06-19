package ru.accouting.student.repository;

import org.springframework.data.repository.CrudRepository;
import ru.accouting.student.model.UserRole;

public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
}

