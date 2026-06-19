package ru.accouting.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.accouting.student.model.Passport;
import ru.accouting.student.model.Student;
import ru.accouting.student.repository.PassportRepository;
import ru.accouting.student.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PassportService {

    private final PassportRepository passportRepository;
    private final StudentRepository studentRepository;

    public List<Passport> findAll() {
        return passportRepository.findAll();
    }

    public Optional<Passport> findById(Long id) {
        return passportRepository.findById(id);
    }

    public Optional<Passport> findByStudentId(Long studentId) {
        return Optional.ofNullable(passportRepository.findByStudentIdStudent(studentId));
    }

    @Transactional
    public Passport createOrUpdateForStudent(Long studentId, Passport passport) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден: " + studentId));

        // Если у студента уже есть паспорт — обновляем его
        Passport existing = passportRepository.findByStudentIdStudent(studentId);
        if (existing != null) {
            existing.setNumberPassport(passport.getNumberPassport());
            existing.setSeriesPassport(passport.getSeriesPassport());
            existing.setPlacePassport(passport.getPlacePassport());
            existing.setDatePassport(passport.getDatePassport());
            return passportRepository.save(existing);
        }

        // Если нет паспорта, то создаём новый
        passport.setStudent(student);
        return passportRepository.save(passport);
    }

    @Transactional
    public void deleteById(Long id) {
        passportRepository.deleteById(id);
    }
}