package ru.accouting.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.accouting.student.model.Platoon;
import ru.accouting.student.model.Student;
import ru.accouting.student.model.StudentStatus;
import ru.accouting.student.repository.PlatoonRepository;
import ru.accouting.student.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlatoonTransferService {

    private final StudentRepository studentRepository;
    private final PlatoonRepository platoonRepository;

    @Transactional
    public int transferStudentsToNextCourse() {
        List<Student> students = studentRepository.findAll();
        int updatedCount = 0;

        for (Student student : students) {
            Platoon currentPlatoon = student.getPlatoon();
            if (currentPlatoon == null || currentPlatoon.getNamePlatoon() == null) {
                continue;
            }

            String currentName = currentPlatoon.getNamePlatoon().trim();
            Optional<Platoon> nextPlatoonOpt = findNextPlatoon(currentName);

            if (nextPlatoonOpt.isPresent()) {
                student.setPlatoon(nextPlatoonOpt.get());
            } else {
                student.setStatus(StudentStatus.GRADUATION);
                student.setPlatoon(null);
            }

            updatedCount++;
        }

        studentRepository.saveAll(students);
        return updatedCount;
    }

    private Optional<Platoon> findNextPlatoon(String currentName) {
        try {
            int currentNumber = Integer.parseInt(currentName);
            int nextNumber = currentNumber + 100;
            String nextName = String.valueOf(nextNumber);

            return platoonRepository.findAll().stream()
                    .filter(p -> nextName.equals(p.getNamePlatoon()))
                    .findFirst();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}