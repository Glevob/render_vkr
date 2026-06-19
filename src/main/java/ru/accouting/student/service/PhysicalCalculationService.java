package ru.accouting.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.accouting.student.model.*;
import ru.accouting.student.repository.ExerciseRepository;
import ru.accouting.student.repository.PhysicalNormRepository;
import ru.accouting.student.repository.PhysicalSumScaleRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhysicalCalculationService {

    private final PhysicalNormRepository normRepository;
    private final PhysicalSumScaleRepository sumScaleRepository;
    private final ExerciseRepository exerciseRepository;

    public int calculateStrengthPoints(PhysicalTraining pt) {
        if (pt.getStrengthExerciseNumber() == null || pt.getStrengthResult() == null) {
            return 0;
        }

        Optional<Exercise> optEx = exerciseRepository.findByNumber(pt.getStrengthExerciseNumber());
        if (optEx.isEmpty()) return 0;
        Exercise ex = optEx.get();

        List<PhysicalNorm> norms = normRepository.findByExercise(ex);
        if (norms.isEmpty()) return 0;

        double result = pt.getStrengthResult(); // Double для совместимости

        if (ex.getResultType() == Exercise.ResultType.REPS) {
            return norms.stream()
                    .filter(n -> n.getValue() != null && n.getValue() <= result)
                    .max(Comparator.comparingDouble(PhysicalNorm::getValue))
                    .map(PhysicalNorm::getPoints)
                    .orElse(0);
        } else {
            return norms.stream()
                    .filter(n -> n.getValue() != null && n.getValue() >= result)
                    .min(Comparator.comparingDouble(PhysicalNorm::getValue))
                    .map(PhysicalNorm::getPoints)
                    .orElse(0);
        }
    }

    public int calculateSpeedPoints(PhysicalTraining pt) {
        if (pt.getSpeedExerciseNumber() == null || pt.getSpeedResult() == null) {
            return 0;
        }

        Optional<Exercise> optEx = exerciseRepository.findByNumber(pt.getSpeedExerciseNumber());
        if (optEx.isEmpty()) return 0;
        Exercise ex = optEx.get();

        List<PhysicalNorm> norms = normRepository.findByExercise(ex);
        if (norms.isEmpty()) return 0;

        double result = pt.getSpeedResult();

        if (ex.getResultType() == Exercise.ResultType.TIME) {
            return norms.stream()
                    .filter(n -> n.getValue() != null && n.getValue() >= result)
                    .min(Comparator.comparingDouble(PhysicalNorm::getValue))
                    .map(PhysicalNorm::getPoints)
                    .orElse(0);
        } else {
            return norms.stream()
                    .filter(n -> n.getValue() != null && n.getValue() <= result)
                    .max(Comparator.comparingDouble(PhysicalNorm::getValue))
                    .map(PhysicalNorm::getPoints)
                    .orElse(0);
        }
    }

    public int calculateEndurancePoints(PhysicalTraining pt) {
        if (pt.getEnduranceExerciseNumber() == null || pt.getEnduranceResult() == null) {
            return 0;
        }

        Optional<Exercise> optEx = exerciseRepository.findByNumber(pt.getEnduranceExerciseNumber());
        if (optEx.isEmpty()) return 0;
        Exercise ex = optEx.get();

        List<PhysicalNorm> norms = normRepository.findByExercise(ex);
        if (norms.isEmpty()) return 0;

        double result = pt.getEnduranceResult();

        if (ex.getResultType() == Exercise.ResultType.TIME) {
            return norms.stream()
                    .filter(n -> n.getValue() != null && n.getValue() >= result)
                    .min(Comparator.comparingDouble(PhysicalNorm::getValue))
                    .map(PhysicalNorm::getPoints)
                    .orElse(0);
        } else {
            return norms.stream()
                    .filter(n -> n.getValue() != null && n.getValue() <= result)
                    .max(Comparator.comparingDouble(PhysicalNorm::getValue))
                    .map(PhysicalNorm::getPoints)
                    .orElse(0);
        }
    }

    public int calculateScaledResult(PhysicalTraining pt, int totalPoints) {
        Student s = pt.getStudent();
        if (s == null) return 0;

        AgeGroup ageGroup = s.isUnder25() ? AgeGroup.UNDER_25 : AgeGroup.OVER_25;

        List<PhysicalSumScale> scales = sumScaleRepository
                .findByAgeGroupAndSumPointsLessThanEqualOrderBySumPointsAsc(ageGroup, totalPoints);

        PhysicalSumScale best = null;
        for (PhysicalSumScale sc : scales) {
            if (sc.getSumPoints() != null && sc.getSumPoints() <= totalPoints) {
                best = sc;
            }
        }
        return best != null ? best.getScaledResult() : 0;
    }
}