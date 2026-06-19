package ru.accouting.student.dto;

public record StudentCredentialsRow(
        String lastName,
        String firstName,
        String patronymic,
        String password
) {}
