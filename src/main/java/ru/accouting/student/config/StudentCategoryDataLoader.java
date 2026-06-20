package ru.accouting.student.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.accouting.student.model.Student;
import ru.accouting.student.repository.StudentRepository;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Order(4)
public class StudentCategoryDataLoader implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) {
            return;
        }

        // Перемешиваем, чтобы выбор был случайным, но с нужными количествами
        Collections.shuffle(students, random);

        // ---------- ПСИХОЛОГИЧЕСКАЯ КАТЕГОРИЯ: IV = 5 ЧЕЛОВЕК ----------

        int psychoIvNeeded = 5;
        int psychoIvAssigned = 0;

        for (Student s : students) {
            if (psychoIvAssigned >= psychoIvNeeded) break;
            if (s.getPsychoCategory() == null) {
                s.setPsychoCategory(Student.PsychoCategory.IV);
                psychoIvAssigned++;
            }
        }

        // Остальным, у кого ещё нет psychoCategory, можно поставить случайно I–III
        for (Student s : students) {
            if (s.getPsychoCategory() == null) {
                // выбираем случайно I, II или III
                Student.PsychoCategory[] allowed =
                        {Student.PsychoCategory.I, Student.PsychoCategory.II, Student.PsychoCategory.III};
                s.setPsychoCategory(allowed[random.nextInt(allowed.length)]);
            }
        }

        // ---------- МЕД. КАТЕГОРИЯ: C = 3, D = 3, E = 1 ----------

        int cNeeded = 3;
        int dNeeded = 3;
        int eNeeded = 1;

        int cAssigned = 0;
        int dAssigned = 0;
        int eAssigned = 0;

        // сначала всем, у кого ещё нет fitnessCategory, ставим A/B (чтобы было безопасное значение)
        for (Student s : students) {
            if (s.getFitnessCategory() == null) {
                Student.FitnessCategory[] good =
                        {Student.FitnessCategory.A, Student.FitnessCategory.B};
                s.setFitnessCategory(good[random.nextInt(good.length)]);
            }
        }

        // снова перемешаем, чтобы выбор C/D/E был равномернее
        Collections.shuffle(students, random);

        // назначаем C
        for (Student s : students) {
            if (cAssigned >= cNeeded) break;
            // меняем только если сейчас не C/D/E (а, скажем, A/B)
            if (s.getFitnessCategory() == Student.FitnessCategory.A ||
                    s.getFitnessCategory() == Student.FitnessCategory.B) {
                s.setFitnessCategory(Student.FitnessCategory.C);
                cAssigned++;
            }
        }

        // назначаем D
        for (Student s : students) {
            if (dAssigned >= dNeeded) break;
            if (s.getFitnessCategory() == Student.FitnessCategory.A ||
                    s.getFitnessCategory() == Student.FitnessCategory.B) {
                s.setFitnessCategory(Student.FitnessCategory.D);
                dAssigned++;
            }
        }

        // назначаем E
        for (Student s : students) {
            if (eAssigned >= eNeeded) break;
            if (s.getFitnessCategory() == Student.FitnessCategory.A ||
                    s.getFitnessCategory() == Student.FitnessCategory.B) {
                s.setFitnessCategory(Student.FitnessCategory.E);
                eAssigned++;
            }
        }

        studentRepository.saveAll(students);
    }
}