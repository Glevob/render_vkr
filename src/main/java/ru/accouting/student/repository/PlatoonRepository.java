package ru.accouting.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.accouting.student.model.MilitaryAccountingSpecialtyEntity;
import ru.accouting.student.model.Platoon;

import java.util.List;

public interface PlatoonRepository extends JpaRepository<Platoon, Long> {
    // Поиск Взвода по наименованию
    List<Platoon> findByNamePlatoon(String namePlatoon);
    // Проверка: есть ли взвод с таким же названием
    boolean existsByNamePlatoon(String namePlatoon);

    boolean existsBySpecialty(MilitaryAccountingSpecialtyEntity specialty);
}
