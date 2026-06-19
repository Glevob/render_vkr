package ru.accouting.student.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.accouting.student.model.SpecialtyCodeInstitute;

import java.util.List;
import java.util.Optional;

public interface SpecialtyCodeInstituteRepository extends JpaRepository<SpecialtyCodeInstitute, Long> {
//    boolean existsByCodeSpecialty(String codeSpeciality);
    // Получение всех специальностей отсортированных
    List<SpecialtyCodeInstitute> findAll(Sort sort);

    // Фильтр по институту + сортировка
    List<SpecialtyCodeInstitute> findByInstitute(String institute, Sort sort);

    // Выбор уникальных институтов + сортировка по алфавиту
    @Query("select distinct s.institute from SpecialtyCodeInstitute s order by s.institute")
    List<String> findDistinctInstitute();

    // Поиск специальности по Коду специальности
    Optional<SpecialtyCodeInstitute> findByCodeSpecialty(String codeSpecialty);

    // Выбор уникальных Кодов специальности + сортировка по алфавиту
    @Query("select distinct s.codeSpecialty from SpecialtyCodeInstitute s order by s.codeSpecialty")
    List<String> findDistinctCodeSpecialty();

    // Поиск Специальности по наименованию
    Optional<SpecialtyCodeInstitute> findByTitleSpecialty(String titleSpecialty);
}
