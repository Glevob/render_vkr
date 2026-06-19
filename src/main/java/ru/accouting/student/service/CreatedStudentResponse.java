package ru.accouting.student.service;

import ru.accouting.student.model.Student;

public record CreatedStudentResponse(Student student, String login, String rawPassword) {
}