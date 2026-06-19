package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.accouting.student.dto.StudentsForm;
import ru.accouting.student.model.Student;
import ru.accouting.student.dto.StudentAssessmentDto;
import ru.accouting.student.repository.StudyGroupRepository;
import ru.accouting.student.service.StudentAssessmentService;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/students/assessment")
@RequiredArgsConstructor
public class StudentAssessmentController {

    private final StudentAssessmentService assessmentService;
    private final StudyGroupRepository studyGroupRepository;

    @GetMapping
    public String showAssessmentPage(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String group,
            @RequestParam(required = false, defaultValue = "group") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            Model model) {

        List<Integer> applicationYears = assessmentService.getApplicationYears();

        // Вычисляем максимальный год
        int maxYear = applicationYears.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(java.time.LocalDate.now().getYear());

        Integer targetYear;   // Год, который пойдет в сервис (null = без фильтра по году)
        Integer selectedYear; // Год, который вернется в HTML для отметки в <select>

        // Четко разделяем три возможных состояния:
        if (year == null) {
            // Первый вход на страницу -> показываем текущий/максимальный год
            targetYear = maxYear;
            selectedYear = maxYear;
        } else if (year == -1) {
            // Осознанный выбор "Все годы" -> убираем фильтр (targetYear = null)
            targetYear = null;
            selectedYear = -1;
        } else {
            // Выбран конкретный год из списка
            targetYear = year;
            selectedYear = year;
        }

        // Запрашиваем данные
        List<StudentAssessmentDto> students = assessmentService.getFilteredForAssessment(targetYear, group);

        // Логика сортировки
        Comparator<StudentAssessmentDto> comparator = switch (sortField) {
            case "fullName" -> Comparator.comparing(StudentAssessmentDto::getFullName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(StudentAssessmentDto::getGroupName, String.CASE_INSENSITIVE_ORDER);
            case "groupName" -> Comparator.comparing(StudentAssessmentDto::getGroupName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(StudentAssessmentDto::getFullName, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(StudentAssessmentDto::getGroupName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(StudentAssessmentDto::getFullName, String.CASE_INSENSITIVE_ORDER);
        };

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        students.sort(comparator);

        // Передаем данные в модель
        model.addAttribute("studentsForm", new StudentsForm(students));
        model.addAttribute("maxYear", maxYear);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedGroup", group);
        model.addAttribute("applicationYears", applicationYears);
        model.addAttribute("groups", studyGroupRepository.findAll(Sort.by("nameGroup")));
        model.addAttribute("fitnessCategories", Student.FitnessCategory.values());
        model.addAttribute("psychoCategories", Student.PsychoCategory.values());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        return "students/assessment";
    }

    @PostMapping
    public String saveAssessments(
            @ModelAttribute("studentsForm") StudentsForm form,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String group,
            RedirectAttributes redirectAttributes) {

        // Сохраняем измененные данные
        assessmentService.saveAll(form.getStudents());

        // Прокидываем текущие фильтры дальше в редирект
        if (year != null) {
            redirectAttributes.addAttribute("year", year);
        }
        if (group != null && !group.isBlank()) {
            redirectAttributes.addAttribute("group", group);
        }

        return "redirect:/students/assessment";
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(2000);
    }

}