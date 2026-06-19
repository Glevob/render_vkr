package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_credentials")
@Getter
@Setter
public class StudentCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_credentials_seq")
    @SequenceGenerator(name = "student_credentials_seq", sequenceName = "student_credentials_seq", allocationSize = 1)
    private Long id;

    // Студент
    @OneToOne
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    // Логин студента
    @Column(nullable = false, length = 50)
    private String login;

    // Пароль стуента
    @Column(nullable = false, length = 255)
    private String rawPassword;

    // Дата создания
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Был ли пароль уже экспортирован?
    @Column(nullable = false)
    private boolean exported = false;
}
