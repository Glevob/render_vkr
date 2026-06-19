package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Entity(name="platoon")
@Table(name="platoon")
public class Platoon {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_platoon_seq")
    @SequenceGenerator(name = "id_platoon_seq", sequenceName = "id_platoon_seq", allocationSize = 1)
    private Long id;

    // Наименование взвода (Например, 101, 203)
    @Column(name = "namePlatoon", nullable = false)
    private String namePlatoon;

    @OneToMany(mappedBy = "platoon")
    private List<Student> students = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "specialty_id")
    private MilitaryAccountingSpecialtyEntity specialty;
}
