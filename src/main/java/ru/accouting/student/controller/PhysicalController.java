package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.accouting.student.dto.PhysicalResultForm;
import ru.accouting.student.dto.PhysicalResultFormItem;
import ru.accouting.student.model.Exercise;
import ru.accouting.student.model.PhysicalTraining;
import ru.accouting.student.model.Student;
import ru.accouting.student.model.StudentStatus;
import ru.accouting.student.repository.*;
import ru.accouting.student.service.PhysicalCalculationService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PhysicalController {

    private final StudentRepository studentRepository;
    private final PhysicalCalculationService physicalCalculationService;
    private final ExerciseRepository exerciseRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final SpecialtyCodeInstituteRepository specialtyCodeInstituteRepository;

    @GetMapping("/physical-list")
    @PreAuthorize("hasAnyAuthority('TECHNOLOGIST','FULL')")
    public String physicalList(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) Integer course,
            @RequestParam(required = false) String institute,
            Model model) {

        // Получаем список всех существующих годов подачи заявления из базы данных
        List<Integer> years = studentRepository.findAll().stream()
                .map(Student::getApplicationYear)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        // Определяем целевой год: если не передан, берем максимальный
        // Если база пустая - берем текущий год системы
        int targetYear;
        if (year != null) {
            targetYear = year;
        } else {
            targetYear = years.isEmpty() ? java.time.LocalDate.now().getYear() : years.get(years.size() - 1);
        }

        // Получение данных
        Specification<Student> spec = StudentSpecifications.withFilters(
                targetYear,  // Год подачи заявления
                group,       // Учебная группа
                course,      // Курс обучения
                null,        // Специальность в ВУЗе
                institute,   // Институт
                null,        // ВУС
                null,        // Взвод
                null,        // Статус в системе
                null         // Поиск
        );
        List<Student> students = studentRepository.findAll(spec, Sort.by("lastName"));

        // Подготовка формы
        PhysicalResultForm form = new PhysicalResultForm();
        List<PhysicalResultFormItem> items = new ArrayList<>();

        for (Student s : students) {
            PhysicalResultFormItem item = new PhysicalResultFormItem();
            item.setStudentId(s.getIdStudent());

            if (s.getPhysicalTraining() != null) {
                PhysicalTraining pt = s.getPhysicalTraining();
                item.setStrengthExerciseNumber(pt.getStrengthExerciseNumber());
                item.setSpeedExerciseNumber(pt.getSpeedExerciseNumber());
                item.setEnduranceExerciseNumber(pt.getEnduranceExerciseNumber());

                item.setStrengthResult(pt.getStrengthResult());
                item.setSpeedResult(pt.getSpeedResult());
                item.setEnduranceResult(pt.getEnduranceResult());
            }
            items.add(item);
        }
        form.setItems(items);

        model.addAttribute("students", students);
        model.addAttribute("form", form);

        // Данные для выпадающих списков
        model.addAttribute("strengthExercises", exerciseRepository.findByCategory(Exercise.ExerciseCategory.STRENGTH));
        model.addAttribute("speedExercises", exerciseRepository.findByCategory(Exercise.ExerciseCategory.SPEED));
        model.addAttribute("enduranceExercises", exerciseRepository.findByCategory(Exercise.ExerciseCategory.ENDURANCE));

        model.addAttribute("years", years);

        // Параметры для отображения состояния
        model.addAttribute("selectedYear", targetYear);
        model.addAttribute("selectedGroup", group);
        model.addAttribute("selectedCourse", course);
        model.addAttribute("selectedInstitute", institute);

        model.addAttribute("groups", studyGroupRepository.findAll(Sort.by("nameGroup")));
        model.addAttribute("institutes", specialtyCodeInstituteRepository.findDistinctInstitute());
        model.addAttribute("courses", List.of(1, 2));

        return "physical-list";
    }

    @PostMapping("/physical-list")
    @PreAuthorize("hasAnyAuthority('TECHNOLOGIST','FULL')")
    public String savePhysical(@ModelAttribute("form") PhysicalResultForm form) {

        for (PhysicalResultFormItem item : form.getItems()) {
            Student s = studentRepository.findById(item.getStudentId()).orElseThrow();

            PhysicalTraining pt = s.getPhysicalTraining();
            if (pt == null) {
                pt = new PhysicalTraining();
                pt.setStudent(s);
                s.setPhysicalTraining(pt);
            }

            pt.setStrengthExerciseNumber(item.getStrengthExerciseNumber());
            pt.setSpeedExerciseNumber(item.getSpeedExerciseNumber());
            pt.setEnduranceExerciseNumber(item.getEnduranceExerciseNumber());

            pt.setStrengthResult(item.getStrengthResult());
            pt.setSpeedResult(item.getSpeedResult());
            pt.setEnduranceResult(item.getEnduranceResult());

            int strengthPts = physicalCalculationService.calculateStrengthPoints(pt);
            int speedPts = physicalCalculationService.calculateSpeedPoints(pt);
            int endurancePts = physicalCalculationService.calculateEndurancePoints(pt);

            pt.setStrengthPoints(strengthPts);
            pt.setSpeedPoints(speedPts);
            pt.setEndurancePoints(endurancePts);

            int total = strengthPts + speedPts + endurancePts;
            pt.setTotalPoints(total);

            int finalResult = physicalCalculationService.calculateScaledResult(pt, total);
            pt.setFinalResult(finalResult);

            recalculateStatusAfterPhysical(s, pt);

            studentRepository.save(s);
        }

        return "redirect:/physical-list";
    }

    // Авто-обновление статуса студента по результатам ФП
    private void recalculateStatusAfterPhysical(Student s, PhysicalTraining pt) {
        boolean allExercisesFilled =
                pt.getStrengthExerciseNumber() != null &&
                        pt.getSpeedExerciseNumber() != null &&
                        pt.getEnduranceExerciseNumber() != null &&
                        pt.getStrengthResult() != null &&
                        pt.getSpeedResult() != null &&
                        pt.getEnduranceResult() != null;

        boolean allExercisesPassed =
                pt.getStrengthPoints() != null && pt.getStrengthPoints() > 0 &&
                        pt.getSpeedPoints() != null && pt.getSpeedPoints() > 0 &&
                        pt.getEndurancePoints() != null && pt.getEndurancePoints() > 0;

        boolean totalPassed = s.isUnder25()
                ? pt.getTotalPoints() != null && pt.getTotalPoints() >= 120
                : pt.getTotalPoints() != null && pt.getTotalPoints() >= 110;

        boolean physicalPassed = allExercisesFilled && allExercisesPassed && totalPassed && pt.getFinalResult() != null && pt.getFinalResult() > 0;

        if (!physicalPassed) {
            s.setStatus(StudentStatus.NOT_PASSED);
            return;
        }

        if ((s.getStatus() == StudentStatus.PRELIMINARY_PASSED || s.getStatus() == StudentStatus.NOT_PASSED) && s.getGrade100() != null && s.getGrade100().compareTo(new java.math.BigDecimal("3.00")) > 0) {
            s.setStatus(StudentStatus.CONTEST_PASSED);
        }
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(500);
    }
}