package ru.accouting.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.accouting.student.model.Passport;

public interface PassportRepository extends JpaRepository<Passport, Long> {
    // Поиск паспорта для студента по id
    Passport findByStudentIdStudent(Long studentId);

    // Проверка наличия такого же хэша паспорта
    boolean existsByPassportHash(String passportHash);
}