package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "study_group")
@Getter
@Setter
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "study_group_seq")
    @SequenceGenerator(name = "study_group_seq", sequenceName = "study_group_seq",
            initialValue = 1, allocationSize = 1)
    @Column(name = "id_group")
    private Long id;

    // Наименование учебной группы (Например, ЦИС-11, МД-13)
    @Column(name = "name_group", nullable = false, unique = true)
    private String nameGroup;

    @Override
    public String toString() {
        return nameGroup;
    }

    // Курс обучения
    @Column(name = "course", nullable = false)
    private Integer course;

    // Специальность учебной группы
    @ManyToOne
    @JoinColumn(name = "specialty_id", nullable = false)
    private SpecialtyCodeInstitute specialty;
}
