package ru.accouting.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.accouting.student.model.StudentCredentials;

import java.util.Optional;
import java.util.List;

@Repository
public interface StudentCredentialsRepository extends JpaRepository<StudentCredentials, Long> {
    // Поиск студента по id студента
    Optional<StudentCredentials> findByStudent_IdStudent(Long idStudent);
//    Optional<StudentCredentials> findByLogin(String login);
//    List<StudentCredentials> findByExportedFalse();

    // Удаление записи по id студента
    void deleteByStudent_IdStudent(Long idStudent);
}