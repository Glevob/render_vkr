package ru.accouting.student.controller;

import ru.accouting.student.dto.RegistrationRequest;
import ru.accouting.student.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationRestController {

    private final UserServiceImpl userService;

    @PostMapping("/registration")
    public ResponseEntity<Void> register(@Valid @RequestBody RegistrationRequest request) {
        userService.registration(request.getLogin(), request.getPassword());
        return ResponseEntity.status(201).build();
    }
}

