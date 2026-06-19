package ru.accouting.student.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ListsMenuController {

    @GetMapping("/lists")
    public String showListsMenu() {
        return "lists-menu";
    }
}

