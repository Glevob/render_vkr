package ru.accouting.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.accouting.student.dto.StudentCredentialsRow;
import ru.accouting.student.model.*;
import ru.accouting.student.repository.StudentCredentialsRepository;
import ru.accouting.student.repository.StudentRepository;
import ru.accouting.student.repository.StudentSpecifications;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentCredentialsRepository studentCredentialsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CreatedStudentResponse addStudent(Student student) {
        String rawPassword = generatePassword(10);

        User user = new User();
        user.setLogin(student.getStudentIdCard());
        user.setPassword(passwordEncoder.encode(rawPassword));

        UserRole role = new UserRole();
        role.setUserAuthority(UserAuthority.USER);
        role.setUser(user);

        user.setUserRoles(List.of(role));
        student.setUser(user);
        user.setStudent(student);

        Student savedStudent = studentRepository.save(student);

        StudentCredentials credentials = new StudentCredentials();
        credentials.setStudent(savedStudent);
        credentials.setLogin(user.getLogin());
        credentials.setRawPassword(rawPassword);
        credentials.setCreatedAt(LocalDateTime.now());
        credentials.setExported(false);

        studentCredentialsRepository.save(credentials);

        return new CreatedStudentResponse(savedStudent, user.getLogin(), rawPassword);
    }

    @Override
    @Transactional
    public List<StudentCredentialsRow> getCredentialsRowsAndDelete(List<Student> students) {
        List<StudentCredentialsRow> rows = students.stream()
                .map(s -> {
                    StudentCredentials creds = studentCredentialsRepository
                            .findByStudent_IdStudent(s.getIdStudent())
                            .orElse(null);

                    return new StudentCredentialsRow(
                            s.getLastName(),
                            s.getFirstName(),
                            s.getPatronymic(),
                            creds != null ? creds.getRawPassword() : ""
                    );
                })
                .toList();

        for (Student s : students) {
            studentCredentialsRepository.deleteByStudent_IdStudent(s.getIdStudent());
        }

        return rows;
    }

    private String generatePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

//    @Override
//    public List<Student> getAllStudents() {
//        return studentRepository.findAll();
//    }

    @Override
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

//    @Override
//    public Optional<Student> putStudentById(Long id, Student updatedStudent) {
//        Optional<Student> existingStudent = studentRepository.findById(id);
//        if (existingStudent.isPresent()) {
//            Student studentToUpdate = existingStudent.get();
//            if (updatedStudent.getFirstName() != null) {
//                studentToUpdate.setFirstName(updatedStudent.getFirstName());
//            }
//            if (updatedStudent.getLastName() != null) {
//                studentToUpdate.setLastName(updatedStudent.getLastName());
//            }
//            studentRepository.save(studentToUpdate);
//        }
//        return existingStudent;
//    }

//    @Override
//    public void deleteStudentById(Long id) {
//        studentRepository.deleteById(id);
//    }

    @Override
    public Page<Student> getFilteredStudents(Integer year, String group, Integer course, String specialty,
                                             String institute, String vus, Long platoonId, StudentStatus status,
                                             String search, Pageable pageable) {
        var spec = StudentSpecifications.withFilters(year, group, course, specialty, institute, vus, platoonId, status, search);
        return studentRepository.findAll(spec, pageable);
    }

}