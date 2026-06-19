package ru.accouting.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.accouting.student.model.MilitaryCommissariatEntity;

public interface MilitaryCommissariatRepository extends JpaRepository<MilitaryCommissariatEntity, Long> {
    // Проверка наличия военного комиссариата по названию
    boolean existsByName(String name);
}
