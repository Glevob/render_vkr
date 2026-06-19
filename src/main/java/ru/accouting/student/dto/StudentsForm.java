package ru.accouting.student.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class StudentsForm {

    private List<StudentAssessmentDto> students;

//    public StudentsForm() {}

    public StudentsForm(List<StudentAssessmentDto> students) {
        this.students = students;
    }

}
