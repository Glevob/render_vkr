package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.accouting.student.model.PhysicalNorm;
import ru.accouting.student.model.PhysicalSumScale;
import ru.accouting.student.repository.ExerciseRepository;
import ru.accouting.student.repository.PhysicalNormRepository;
import ru.accouting.student.repository.PhysicalSumScaleRepository;

@Controller
@RequestMapping("/admin-physical")
@RequiredArgsConstructor
public class AdminPhysicalController {

    private final PhysicalNormRepository normRepository;
    private final PhysicalSumScaleRepository sumScaleRepository;
    private final ExerciseRepository exerciseRepository;

    // Постраничные списки Упражнений и Норм
    @GetMapping
    @PreAuthorize("hasAuthority('FULL')")
    public String adminPhysicalPage(
            @RequestParam(defaultValue = "0") int norm_page,
            @RequestParam(defaultValue = "0") int scale_page,
            Model model) {

        model.addAttribute("normPage", normRepository.findAll(PageRequest.of(norm_page, 10)));
        model.addAttribute("scalePage", sumScaleRepository.findAll(PageRequest.of(scale_page, 10)));

        model.addAttribute("newNorm", new PhysicalNorm());
        model.addAttribute("newScale", new PhysicalSumScale());
        model.addAttribute("exercises", exerciseRepository.findAll());
        return "admin-physical";
    }

    @PostMapping("/norms")
    @PreAuthorize("hasAuthority('FULL')")
    public String addNorm(@ModelAttribute PhysicalNorm newNorm) {
        normRepository.save(newNorm);
        return "redirect:/admin-physical";
    }

    @PostMapping("/norms/delete/{id}")
    @PreAuthorize("hasAuthority('FULL')")
    public String deleteNorm(@PathVariable Long id) {
        normRepository.deleteById(id);
        return "redirect:/admin-physical";
    }

    @PostMapping("/scale")
    @PreAuthorize("hasAuthority('FULL')")
    public String addScale(@ModelAttribute PhysicalSumScale newScale) {
        sumScaleRepository.save(newScale);
        return "redirect:/admin-physical";
    }

    @PostMapping("/scale/delete/{id}")
    @PreAuthorize("hasAuthority('FULL')")
    public String deleteScale(@PathVariable Long id) {
        sumScaleRepository.deleteById(id);
        return "redirect:/admin-physical";
    }
}
