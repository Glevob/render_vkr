package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.accouting.student.model.Passport;
import ru.accouting.student.service.PassportService;
import ru.accouting.student.service.StudentService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/passports")
public class PassportController {

    private final PassportService passportService;
    private final StudentService studentService;

    // Список паспортов
    @GetMapping
    public String listPassports(Model model) {
        model.addAttribute("passports", passportService.findAll());
        return "passport/passport-list";
    }

    // Форма добавления/изменения паспорта для конкретного студента
    @GetMapping("/student/{studentId}/edit")
    public String editPassportForStudent(@PathVariable Long studentId, Model model) {
        var student = studentService.getStudentById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));

        var passportOpt = passportService.findByStudentId(studentId);
        Passport passport = passportOpt.orElseGet(Passport::new);

        model.addAttribute("student", student);
        model.addAttribute("passport", passport);
        return "passport/passport-form";
    }

    // Сохранение паспорта
    @PostMapping("/student/{studentId}/save")
    public String savePassportForStudent(@PathVariable Long studentId,
                                         @ModelAttribute("passport") Passport passport) {
        passportService.createOrUpdateForStudent(studentId, passport);
        return "redirect:/passports";
    }

    // Удаление паспорта
    @GetMapping("/delete/{id}")
    public String deletePassport(@PathVariable Long id) {
        passportService.deleteById(id);
        return "redirect:/passports";
    }
}