package ru.accouting.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.accouting.student.model.AgeGroup;
import ru.accouting.student.model.PhysicalSumScale;

import java.util.List;

public interface PhysicalSumScaleRepository extends JpaRepository<PhysicalSumScale, Long> {

    // Поиск записи по возрастной группе, у которых сумма баллов меньше или равна переданному значению
    // то есть берём запись с sumPoints <= totalPoints, максимально близкую к totalPoints
    // А затем отсортировать во возрастанию баллов
    List<PhysicalSumScale> findByAgeGroupAndSumPointsLessThanEqualOrderBySumPointsAsc(
            AgeGroup ageGroup,
            Integer sumPoints
    );

    // Для проверки существующих данных
//    long count();
}