package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "physical_training")
@Getter
@Setter
public class PhysicalTraining {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "student_id", unique = true, nullable = false)
    private Student student;

    // Результат Упражнения на Силу
    @Column(name = "strength_result")
    private Integer strengthResult;

    // Балл за Упражнение на Силу (100-балльная)
    @Column(name = "strength_points")
    private Integer strengthPoints;

    // Результат Упражнения на Быстроту
    @Column(name = "speed_result")
    private Double speedResult;

    // Балл за Упражнение на Быстроту (100-балльная)
    @Column(name = "speed_points")
    private Integer speedPoints;

    // Результат Упражнения на Выносливость
    @Column(name = "endurance_result")
    private Double enduranceResult;

    // Балл за Упражнение на Выносливость (100-балльная)
    @Column(name = "endurance_points")
    private Integer endurancePoints;

    // Сумма баллов за Упражнения
    @Column(name = "total_points")
    private Integer totalPoints;

    // Сумма баллов за Упражнения, переведенная в 100-балльную шкалу
    @Column(name = "final_result")
    private Integer finalResult;

    // Номер упражнения на Силу
    @Column(name = "strength_exercise_number")
    private Integer strengthExerciseNumber;

    // Номер упражнения на Быстроту
    @Column(name = "speed_exercise_number")
    private Integer speedExerciseNumber;

    // Номер упражнения на Выносливость
    @Column(name = "endurance_exercise_number")
    private Integer enduranceExerciseNumber;
}