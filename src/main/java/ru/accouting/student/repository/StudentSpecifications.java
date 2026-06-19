package ru.accouting.student.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.accouting.student.model.Student;
import jakarta.persistence.criteria.Predicate;
import ru.accouting.student.model.StudentStatus;

import java.util.ArrayList;
import java.util.List;

public class StudentSpecifications {

    public static Specification<Student> withFilters(
            Integer year,         // год подачи заявления
            String group,         // Учебная группа
            Integer course,       // Курс обучения
            String specialty,     // Специальность в ВУЗе
            String institute,     // Институт
            String vus,           // ВУС
            Long platoonId,       // Взвод
            StudentStatus status, // Статутс в системе
            String search         // Поиск
    ) {
        return (root, query, cb) -> {
            // Динамический список условий
            List<Predicate> predicates = new ArrayList<>();

            // Фильтры по параметрам
            if (year != null) predicates.add(cb.equal(root.get("applicationYear"), year));
            if (group != null && !group.isBlank()) predicates.add(cb.equal(root.get("groupStudent").get("nameGroup"), group));
            if (course != null) predicates.add(cb.equal(root.get("course"), course));
            if (specialty != null && !specialty.isBlank()) predicates.add(cb.equal(root.get("groupStudent").get("specialty").get("titleSpecialty"), specialty));
            if (institute != null && !institute.isBlank()) predicates.add(cb.equal(root.get("groupStudent").get("specialty").get("institute"), institute));
            if (vus != null && !vus.isBlank()) predicates.add(cb.equal(root.get("militaryAccountingSpecialty").get("code"), vus));
            if (platoonId != null) predicates.add(cb.equal(root.get("platoon").get("id"), platoonId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));

            // Поиск по ФИО
            // Если в поисковую строку ввести "Петров Иван", "Иван Петров" или "Иван Сидорович",
            // алгоритм найдет студента, так как он проверяет вхождение каждого слова отдельно,
            // независимо от того, в каком порядке их ввел пользователь
            if (search != null && !search.isBlank()) {
                // Разбиваем строку по пробелам на отдельные слова
                String[] tokens = search.toLowerCase().trim().split("\\s+");

                for (String token : tokens) {
                    String pattern = "%" + token + "%";
                    // Для каждого слова требуем, чтобы оно было найдено хотя бы в одном из полей
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("lastName")), pattern),
                            cb.like(cb.lower(root.get("firstName")), pattern),
                            cb.like(cb.lower(root.get("patronymic")), pattern)
                    ));
                }
            }
            // Объединение всех параметров поиска через AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}