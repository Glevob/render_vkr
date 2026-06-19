package ru.accouting.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.accouting.student.model.Student;
import ru.accouting.student.model.StudentStatus;
import ru.accouting.student.repository.StudentRepository;

@Service
@RequiredArgsConstructor
public class StudentStatusService {


    public void recalculateStatus(Student student) {
        if (student == null) return;

        if (student.getPsychoCategory() == null) {
            student.setStatus(StudentStatus.APPLIED);
            return;
        }

        boolean psychoOk = student.getPsychoCategory() == Student.PsychoCategory.I
                || student.getPsychoCategory() == Student.PsychoCategory.II
                || student.getPsychoCategory() == Student.PsychoCategory.III;

        boolean physicalOk = student.getPhysicalTraining() != null
                && student.getPhysicalTraining().getStrengthResult() != null
                && student.getPhysicalTraining().getSpeedResult() != null
                && student.getPhysicalTraining().getEnduranceResult() != null
                && student.getPhysicalTraining().getStrengthPoints() != null
                && student.getPhysicalTraining().getSpeedPoints() != null
                && student.getPhysicalTraining().getEndurancePoints() != null
                && student.getPhysicalTraining().getTotalPoints() != null
                && student.getPhysicalTraining().getTotalPoints() > 0;

        boolean academicOk = student.getGrade100() != null && student.getGrade100().doubleValue() >= 3.00;

        if (!psychoOk || !physicalOk || !academicOk) {
            student.setStatus(StudentStatus.NOT_PASSED);
            return;
        }

        student.setStatus(StudentStatus.CONTEST_PASSED);
    }
}