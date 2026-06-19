package ru.accouting.student.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.accouting.student.dto.StudentCredentialsRow;
import ru.accouting.student.model.Student;
import ru.accouting.student.model.StudentStatus;

import java.util.List;
import java.util.Optional;

public interface StudentService {

    CreatedStudentResponse addStudent(Student student);

//    List<Student> getAllStudents();
//    Optional<Student> putStudentById(Long id, Student updateStudent);
//    void deleteStudentById(Long id);

    Optional<Student> getStudentById(Long id);

    List<StudentCredentialsRow> getCredentialsRowsAndDelete(List<Student> students);

    Page<Student> getFilteredStudents(Integer year, String group, Integer course, String specialty,
                                      String institute, String vus, Long platoonId, StudentStatus status,
                                      String search, Pageable pageable);

}
