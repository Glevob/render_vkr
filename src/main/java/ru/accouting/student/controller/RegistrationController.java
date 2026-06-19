package ru.accouting.student.controller;

import ru.accouting.student.dto.RegistrationRequest;
import ru.accouting.student.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserServiceImpl userService;

    @GetMapping("/registration")
    public String registrationPage(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@Valid RegistrationRequest registrationRequest,
                           BindingResult bindingResult,
                           Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("registrationRequest", registrationRequest);
            return "registration";
        }

        userService.registration(
                registrationRequest.getLogin(),
                registrationRequest.getPassword()
        );

        return "redirect:/enter";
    }
}
