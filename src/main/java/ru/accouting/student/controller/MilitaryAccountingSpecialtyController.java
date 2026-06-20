package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.accouting.student.model.MilitaryAccountingSpecialtyEntity;
import ru.accouting.student.repository.MilitaryAccountingSpecialtyEntityRepository;
import ru.accouting.student.repository.PlatoonRepository;
import ru.accouting.student.repository.StudentRepository;

@Controller
@RequiredArgsConstructor
@RequestMapping("/military-accounting-specialties")
public class MilitaryAccountingSpecialtyController {

    private final MilitaryAccountingSpecialtyEntityRepository repository;
    private final StudentRepository studentRepository;
    private final PlatoonRepository platoonRepository;

    // Список + сортировка + сообщение/связанные студенты
    @GetMapping
    public String list(@RequestParam(name = "sortField", required = false, defaultValue = "code") String sortField,
                       @RequestParam(name = "sortDir", required = false, defaultValue = "asc") String sortDir,
                       @RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "vusId", required = false) Long vusId,
                       Model model) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        model.addAttribute("specialties", repository.findAll(sort));
        model.addAttribute("errorMessage", error);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        if (vusId != null) {
            MilitaryAccountingSpecialtyEntity vus = repository.findById(vusId).orElse(null);
            if (vus != null) {
                model.addAttribute("linkedStudents",
                        studentRepository.findByMilitaryAccountingSpecialty(vus));
            }
        }

        return "military-accounting-specialty-list";
    }

    // Страница создания ВУС
    @GetMapping("/add")
    public String showCreateForm(Model model) {
        model.addAttribute("specialty", new MilitaryAccountingSpecialtyEntity());
        return "military-accounting-specialty-add";
    }

    // Сохранение нового ВУС
    @PostMapping("/add")
    public String create(@ModelAttribute("specialty") MilitaryAccountingSpecialtyEntity specialty,
                         BindingResult bindingResult) {
        // Проверяем, занят ли код
        if (repository.findByCode(specialty.getCode()).isPresent()) {
            bindingResult.rejectValue("code", "error.specialty", "ВУС с таким кодом уже существует");
        }
        // Если есть ошибки - возвращаем пользователя на форму создания
        if (bindingResult.hasErrors()) {
            return "military-accounting-specialty-add";
        }
        repository.save(specialty);
        return "redirect:/military-accounting-specialties";
    }

    // Страница редактирования ВУС
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        MilitaryAccountingSpecialtyEntity specialty = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ВУС не найден: " + id));

        model.addAttribute("specialty", specialty);
        return "military-accounting-specialty-edit";
    }

    // Редактирование ВУС
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("specialty") MilitaryAccountingSpecialtyEntity formSpecialty,
                         BindingResult bindingResult) {

        repository.findByCode(formSpecialty.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                bindingResult.rejectValue("code", "error.specialty", "Этот код уже присвоен другой специальности");
            }
        });

        if (bindingResult.hasErrors()) {
            return "military-accounting-specialty-edit";
        }

        MilitaryAccountingSpecialtyEntity specialty = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ВУС не найден: " + id));

        specialty.setCode(formSpecialty.getCode());
        specialty.setTitle(formSpecialty.getTitle());

        repository.save(specialty);
        return "redirect:/military-accounting-specialties";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        MilitaryAccountingSpecialtyEntity vus = repository.findById(id).orElseThrow();

        // Проверяем, существуют ли взводы, связанные с этим ВУС
        boolean hasPlatoons = platoonRepository.existsBySpecialty(vus);

        if (hasPlatoons) {
            // Дополнительно смотрим, есть ли в системе студенты с этим ВУС
            boolean hasStudents = studentRepository.existsByMilitaryAccountingSpecialty(vus);

            if (hasStudents) {
                redirectAttributes.addAttribute("error",
                        "Невозможно удалить ВУС: с ним связаны активные студенты.");
            } else {
                // Ситуация, когда взводы пустые, но они есть
                redirectAttributes.addAttribute("error",
                        "Невозможно удалить ВУС: за ним всё ещё закреплены учебные взводы. Сначала удалите или перенесите эти взводы.");
            }

            redirectAttributes.addAttribute("vusId", id);
            return "redirect:/military-accounting-specialties";
        }

        // Если ни взводов, ни студентов нет - удаляем
        repository.delete(vus);
        return "redirect:/military-accounting-specialties";
    }
}
