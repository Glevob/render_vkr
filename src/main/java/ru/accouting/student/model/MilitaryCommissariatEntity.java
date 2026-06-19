package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "military_commissariat")
@Getter
@Setter
public class MilitaryCommissariatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mil_comm_seq")
    @SequenceGenerator(name = "mil_comm_seq", sequenceName = "mil_comm_seq",
            initialValue = 1, allocationSize = 1)
    @Column(name = "id_military_commissariat")
    private Long id;

    // Наименование военного комиссариата
    @Column(name = "name_military_commissariat", nullable = false, unique = true, length = 255)
    private String name;
}
