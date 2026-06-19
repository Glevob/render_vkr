package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.accouting.student.model.MilitaryCommissariatEntity;
import ru.accouting.student.repository.MilitaryCommissariatRepository;
import ru.accouting.student.repository.StudentRepository;

@Controller
@RequiredArgsConstructor
@RequestMapping("/military-commissariats")
public class MilitaryCommissariatController {

    private final MilitaryCommissariatRepository commissariatRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    public String list(@RequestParam(name = "sortField", required = false, defaultValue = "name") String sortField,
                       @RequestParam(name = "sortDir", required = false, defaultValue = "asc") String sortDir,
                       @RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "commissariatId", required = false) Long commissariatId,
                       Model model) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        model.addAttribute("commissariats", commissariatRepository.findAll(sort));
        model.addAttribute("errorMessage", error);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        if (commissariatId != null) {
            MilitaryCommissariatEntity commissariat =
                    commissariatRepository.findById(commissariatId).orElse(null);
            if (commissariat != null) {
                model.addAttribute("linkedStudents",
                        studentRepository.findByMilitaryCommissariat(commissariat));
            }
        }

        return "military-commissariat-list";
    }

    // Страница создания военкомата
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("commissariat", new MilitaryCommissariatEntity());
        return "military-commissariat-add";
    }

    // Сохранение нового военного комиссариата
    @PostMapping("/add")
    public String create(@ModelAttribute("commissariat") MilitaryCommissariatEntity commissariat,
                         BindingResult bindingResult) {

        // Проверяем, что имя введено и уже существует в БД
        if (commissariat.getName() != null && !commissariat.getName().trim().isEmpty()) {
            if (commissariatRepository.existsByName(commissariat.getName().trim())) {
                bindingResult.rejectValue("name", "error.commissariat", "Военкомат с таким названием уже существует");
            }
        }

        // Если обнаружены ошибки — возвращаем на ту же форму
        if (bindingResult.hasErrors()) {
            return "military-commissariat-add";
        }

        // Если всё прошло - сохраняем
        commissariatRepository.save(commissariat);
        return "redirect:/military-commissariats";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        MilitaryCommissariatEntity commissariat =
                commissariatRepository.findById(id).orElseThrow();

        if (studentRepository.existsByMilitaryCommissariat(commissariat)) {
            redirectAttributes.addAttribute("error",
                    "Невозможно удалить военкомат: с ним связаны студенты.");
            redirectAttributes.addAttribute("commissariatId", id);
            return "redirect:/military-commissariats";
        }

        commissariatRepository.delete(commissariat);
        return "redirect:/military-commissariats";
    }
}
