package ru.accouting.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.accouting.student.model.AgeGroup;
import ru.accouting.student.model.Exercise;
import ru.accouting.student.model.PhysicalNorm;
import ru.accouting.student.model.ResultType;

import java.util.Collection;
import java.util.List;

public interface PhysicalNormRepository extends JpaRepository<PhysicalNorm, Long> {

    // сила / скорость / выносливость — ищем норму по: номеру упражнения, возрасту и результату
    // для COUNT — ищем максимальную норму, у которой value <= фактического результата
//    List<PhysicalNorm> findByExerciseNumberAndAgeGroupInAndResultTypeOrderByValueAsc(
//            Integer exerciseNumber,
//            Collection<AgeGroup> ageGroups,
//            ResultType resultType
//    );

    // Поиск Норм по Упражнению
    List<PhysicalNorm> findByExercise(Exercise exercise);
}

