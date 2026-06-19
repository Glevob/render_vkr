package ru.accouting.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.accouting.student.model.MilitaryAccountingSpecialtyEntity;

import java.util.Optional;

public interface MilitaryAccountingSpecialtyEntityRepository extends JpaRepository<MilitaryAccountingSpecialtyEntity, Long> {
//    boolean existsByCode(String code);//
    // Поиск ВУС по коду
    Optional<MilitaryAccountingSpecialtyEntity> findByCode(String code);
}
