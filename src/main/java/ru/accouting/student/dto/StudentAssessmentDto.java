package ru.accouting.student.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;
import ru.accouting.student.model.Student;

import java.math.BigDecimal;

@Getter
@Setter
public class StudentAssessmentDto {

    private Long id;
    private String fullName;
    private String groupName;

    private Student.FitnessCategory fitnessCategory;
    private Student.PsychoCategory psychoCategory;

    @Digits(integer = 1, fraction = 2)
    @DecimalMin("2.00")
    @DecimalMax("5.00")
    private BigDecimal grade5;

    private BigDecimal grade100;
}
