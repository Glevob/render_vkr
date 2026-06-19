package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "specialty")
@Getter
@Setter
public class SpecialtyCodeInstitute {

    @Id
    @Column(name="id_specialty")
    @GeneratedValue(generator = "id_specialty_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "id_specialty_seq", sequenceName = "id_specialty_seq", initialValue = 1, allocationSize = 1)
    private Long id;

    // Код специальности в ВУЗе (Например, 09.03.02)
    @Column(name = "codeSpecialty", nullable = false, unique = true, length = 8)
    private String codeSpecialty;

    // Наименование специальности в ВУЗе (Например, Информационные системы и технологии)
    @Column(name = "titleSpecialty", nullable = false)
    private String titleSpecialty;

    // Институт
    @Column(name = "institute", nullable = false, length = 20)
    private String institute;

    public SpecialtyCodeInstitute() { }

    public SpecialtyCodeInstitute(String codeSpecialty, String titleSpecialty, String institute) {
        this.codeSpecialty = codeSpecialty;
        this.titleSpecialty = titleSpecialty;
        this.institute = institute;
    }

//    public String getDisplayName() {
//        return codeSpecialty + " - " + titleSpecialty;
//    }
}
