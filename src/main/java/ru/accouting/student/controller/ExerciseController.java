package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.accouting.student.model.Exercise;
import ru.accouting.student.repository.ExerciseRepository;

import java.util.List;

@Controller
@RequestMapping("/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseRepository exerciseRepository;


    @GetMapping
    public String listExercises(Model model) {
        List<Exercise> exercises = exerciseRepository.findAll();
        model.addAttribute("exercises", exercises);
        return "exercises/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("exercise", new Exercise());
        model.addAttribute("categories", Exercise.ExerciseCategory.values());
        model.addAttribute("resultTypes", Exercise.ResultType.values());
        model.addAttribute("ageGroups", Exercise.AgeGroup.values());
        return "exercises/new";
    }

    @PostMapping("/new")
    public String createExercise(@ModelAttribute("exercise") Exercise exercise) {
        exerciseRepository.save(exercise);
        return "redirect:/exercises/new";
    }

    @PostMapping("/delete/{id}")
    public String deleteExercise(@PathVariable Long id) {
        exerciseRepository.deleteById(id);
        return "redirect:/exercises";
    }
}