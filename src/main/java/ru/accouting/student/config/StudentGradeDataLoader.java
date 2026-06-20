package ru.accouting.student.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.accouting.student.model.Student;
import ru.accouting.student.repository.StudentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Order(3)
public class StudentGradeDataLoader implements CommandLineRunner {

    private final StudentRepository studentRepository;

    public StudentGradeDataLoader(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {

        if (studentRepository.count() == 0) {
            return;
        }

        List<Student> students = studentRepository.findAll();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (Student s : students) {

            // 1) случайная оценка 3.00–5.00 (BigDecimal)
            BigDecimal grade5 = randomBigDecimal(rnd, 3.00, 5.00, 2);
            s.setGrade5(grade5);

            // 2) перевод в 100‑балльную по той же формуле, что в JS convertTo100
            BigDecimal grade100 = convertTo100(grade5);
            s.setGrade100(grade100);

            studentRepository.save(s);
        }
    }

    /**
     * Случайное BigDecimal в [min, max] с указанным количеством знаков после запятой.
     */
    private BigDecimal randomBigDecimal(ThreadLocalRandom rnd,
                                        double min,
                                        double max,
                                        int scale) {
        double value = min + (max - min) * rnd.nextDouble();
        return BigDecimal
                .valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Аналог JS convertTo100, но для BigDecimal.
     * 3.00 и ниже -> 0.00
     * 5.00 и выше -> 100.00
     * между ними шаг 0.02 даёт 1..99.
     */
    private BigDecimal convertTo100(BigDecimal grade5) {
        BigDecimal three = BigDecimal.valueOf(3.0);
        BigDecimal five = BigDecimal.valueOf(5.0);

        if (grade5.compareTo(three) <= 0) {
            return BigDecimal.valueOf(0.00).setScale(2, RoundingMode.HALF_UP);
        }
        if (grade5.compareTo(five) >= 0) {
            return BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
        }

        // (g - 3.0) / 0.02 -> steps
        BigDecimal step = BigDecimal.valueOf(0.02);
        BigDecimal diff = grade5.subtract(three);
        long steps = diff.divide(step, 0, RoundingMode.HALF_UP).longValue(); // 0..100

        return BigDecimal
                .valueOf(steps)
                .setScale(2, RoundingMode.HALF_UP);
    }
}