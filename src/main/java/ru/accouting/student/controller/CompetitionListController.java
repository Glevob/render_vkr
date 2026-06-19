package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.accouting.student.model.Student;
import ru.accouting.student.repository.SpecialtyCodeInstituteRepository;
import ru.accouting.student.repository.StudentRepository;
import ru.accouting.student.repository.StudyGroupRepository;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CompetitionListController {

    private final StudentRepository studentRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final SpecialtyCodeInstituteRepository specialtyCodeInstituteRepository;

    // Конкурсный список студентов с фильтрами и сортировкой.
    @GetMapping("/contest-list")
    public String competitionList(
            // Сортировка
            @RequestParam(name = "sortField", required = false, defaultValue = "lastName") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "asc") String sortDir,

            // Фильтры
            @RequestParam(name = "specialtyCode", required = false) String specialtyCode,
            @RequestParam(name = "institute", required = false) String institute,
            @RequestParam(name = "course", required = false) Integer course,
            @RequestParam(name = "group", required = false) String groupName,
            @RequestParam(name = "hasNote", required = false) String hasNote,

            Model model
    ) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        List<Student> students;

        boolean hasFilter =
                (specialtyCode != null && !specialtyCode.isBlank()) ||
                        (institute != null && !institute.isBlank()) ||
                        course != null ||
                        (groupName != null && !groupName.isBlank()) ||
                        (hasNote != null && !hasNote.isBlank());

        if (hasFilter) {
            students = studentRepository.findByCompetitionFilters(
                    specialtyCode,
                    institute,
                    course,
                    groupName,
                    hasNote,
                    sort
            );
        } else {
            students = studentRepository.findAll(sort);
        }

        model.addAttribute("students", students);

        // Данные для фильтров
        model.addAttribute("specialtyCodes",
                specialtyCodeInstituteRepository.findDistinctCodeSpecialty());
        model.addAttribute("institutes",
                specialtyCodeInstituteRepository.findDistinctInstitute());
        model.addAttribute("courses", List.of(1, 2));
        model.addAttribute("groups",
                studyGroupRepository.findAll(Sort.by("nameGroup")));

        // Выбранные значения фильтров
        model.addAttribute("selectedSpecialtyCode", specialtyCode);
        model.addAttribute("selectedInstitute", institute);
        model.addAttribute("selectedCourse", course);
        model.addAttribute("selectedGroup", groupName);
        model.addAttribute("selectedHasNote", hasNote);

        // Сортировка
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "competition-list";
    }
}
