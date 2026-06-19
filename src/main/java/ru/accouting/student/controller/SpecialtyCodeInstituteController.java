package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.accouting.student.model.SpecialtyCodeInstitute;
import ru.accouting.student.model.StudyGroup;
import ru.accouting.student.repository.SpecialtyCodeInstituteRepository;
import ru.accouting.student.repository.StudentRepository;
import ru.accouting.student.repository.StudyGroupRepository;

import java.util.List;

@Controller
@RequestMapping("/specialties")
@RequiredArgsConstructor
public class SpecialtyCodeInstituteController {

    private final SpecialtyCodeInstituteRepository specialtyRepo;
    private final StudentRepository studentRepository;
    private final StudyGroupRepository studyGroupRepository;

    // Список + сортировка + фильтр + сообщение об ошибке и связанные студенты
    @GetMapping
    public String listSpecialties(
            @RequestParam(name = "sortField", required = false, defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "asc") String sortDir,
            @RequestParam(name = "institute", required = false) String institute,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "specialtyId", required = false) Long specialtyId,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        List<SpecialtyCodeInstitute> specialties;

        if (institute != null && !institute.isBlank()) {
            specialties = specialtyRepo.findByInstitute(institute, sort);
        } else {
            specialties = specialtyRepo.findAll(sort);
        }

        List<String> institutes = specialtyRepo.findDistinctInstitute();

        model.addAttribute("specialties", specialties);
        model.addAttribute("institutes", institutes);
        model.addAttribute("selectedInstitute", institute);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        // Сообщение об ошибке при удалении
        model.addAttribute("errorMessage", error);

        // Связанные студенты, если была попытка удаления
        if (specialtyId != null) {
            SpecialtyCodeInstitute specialty =
                    specialtyRepo.findById(specialtyId).orElse(null);
            if (specialty != null) {
                // Сначала находим группы этой специальности,
                // Потом студентов этих групп
                List<StudyGroup> groups = studyGroupRepository.findBySpecialty(specialty);
                model.addAttribute("linkedStudents",
                        studentRepository.findByGroupStudentIn(groups));
            }
        }

        return "specialtyCodeInstitute-list";
    }

    // Форма добавления
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("specialty", new SpecialtyCodeInstitute());
        model.addAttribute("formTitle", "Добавление специальности");
        return "specialtyCodeInstitute-form";
    }

    // Сохранение новой специальности
    @PostMapping
    public String createSpecialty(@ModelAttribute("specialty") SpecialtyCodeInstitute specialty,
                                  BindingResult bindingResult,
                                  Model model) {

        // Проверяем уникальность кода
        if (specialty.getCodeSpecialty() != null && specialtyRepo.findByCodeSpecialty(specialty.getCodeSpecialty().trim()).isPresent()) {
            bindingResult.rejectValue("codeSpecialty", "error.specialty", "Специальность с таким кодом уже существует");
        }

        // Проверяем уникальность названия
        if (specialty.getTitleSpecialty() != null && specialtyRepo.findByTitleSpecialty(specialty.getTitleSpecialty().trim()).isPresent()) {
            bindingResult.rejectValue("titleSpecialty", "error.specialty", "Специальность с таким названием уже существует");
        }

        // Если есть ошибки — возвращаем на форму создания
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Добавление специальности");
            return "specialtyCodeInstitute-form";
        }

        specialtyRepo.save(specialty);
        return "redirect:/specialties";
    }

    // Форма редактирования
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        SpecialtyCodeInstitute specialty = specialtyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Специальность не найдена: " + id));

        model.addAttribute("specialty", specialty);
        model.addAttribute("formTitle", "Редактирование специальности");
        return "specialtyCodeInstitute-form";
    }

    // Редактирование специальности
    @PostMapping("/{id}")
    public String updateSpecialty(@PathVariable Long id,
                                  @ModelAttribute("specialty") SpecialtyCodeInstitute formSpecialty,
                                  BindingResult bindingResult,
                                  Model model) {

        // Проверяем уникальность кода (игнорируя текущую запись)
        if (formSpecialty.getCodeSpecialty() != null) {
            specialtyRepo.findByCodeSpecialty(formSpecialty.getCodeSpecialty().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    bindingResult.rejectValue("codeSpecialty", "error.specialty", "Этот код уже присвоен другой специальности");
                }
            });
        }

        // Проверяем уникальность названия (игнорируя текущую запись)
        if (formSpecialty.getTitleSpecialty() != null) {
            specialtyRepo.findByTitleSpecialty(formSpecialty.getTitleSpecialty().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    bindingResult.rejectValue("titleSpecialty", "error.specialty", "Это название уже используется");
                }
            });
        }

        // Если есть ошибки — возвращаем на форму редактирования
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Редактирование специальности");
            return "specialtyCodeInstitute-form";
        }

        SpecialtyCodeInstitute specialty = specialtyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Специальность не найдена: " + id));

        specialty.setCodeSpecialty(formSpecialty.getCodeSpecialty());
        specialty.setTitleSpecialty(formSpecialty.getTitleSpecialty());
        specialty.setInstitute(formSpecialty.getInstitute());

        specialtyRepo.save(specialty);
        return "redirect:/specialties";
    }

    // Удаление с проверкой связей
    @PostMapping("/{id}/delete")
    public String deleteSpecialty(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        SpecialtyCodeInstitute specialty =
                specialtyRepo.findById(id).orElseThrow(() ->
                        new IllegalArgumentException("Специальность не найдена: " + id));

        // проверяем, есть ли группы и студенты с этой специальностью
        List<StudyGroup> groups = studyGroupRepository.findBySpecialty(specialty);
        boolean hasStudents = !groups.isEmpty()
                && studentRepository.existsByGroupStudentIn(groups);

        if (hasStudents) {
            redirectAttributes.addAttribute("error",
                    "Невозможно удалить специальность: с ней связаны студенты.");
            redirectAttributes.addAttribute("specialtyId", id);
            return "redirect:/specialties";
        }

        specialtyRepo.delete(specialty);
        return "redirect:/specialties";
    }
}