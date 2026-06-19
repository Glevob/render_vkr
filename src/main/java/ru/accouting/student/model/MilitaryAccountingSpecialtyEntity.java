package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "military_accounting_specialty")
@Getter
@Setter
public class MilitaryAccountingSpecialtyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mil_acc_spec_seq")
    @SequenceGenerator(name = "mil_acc_spec_seq", sequenceName = "mil_acc_spec_seq",
            initialValue = 1, allocationSize = 1)
    @Column(name = "id_military_accounting_specialty")
    private Long id;

    // Код ВУС (Например, 225543)
    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    // Наименование ВУС (Например, Беспилотные летательные аппараты. Оператор (солдаты запаса))
    @Column(name = "title", nullable = false, length = 255)
    private String title;
}
