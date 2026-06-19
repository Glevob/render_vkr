package ru.accouting.student.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.accouting.student.model.*;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    // Поиск студента по номеру суденческого билета
    Optional<Student> findByStudentIdCard(String studentIdCard);

    // Поиск студента по номеру телефона
    Optional<Student> findByPhoneNumber(String phoneNumber);

    // Поиск студента по номеру и серии паспорта
    Optional<Student> findByPassport_SeriesPassportAndPassport_NumberPassport(String series, String number);

    // Проверка уникальности телефона
    boolean existsByPhoneNumber(String phoneNumber);

    // Проверка уникальности связки серия + номер паспорта
//    boolean existsByPassport_SeriesPassportAndPassport_NumberPassport(String series, String number);

    // Проверка уникальности студенческого билета
    boolean existsByStudentIdCard(String studentIdCard);

    // Поиск всех студентов
    List<Student> findAll();

    // Поиск по статусу в системе
    List<Student> findByStatus(StudentStatus studentStatus);

    // Проверка наличия студентов по ВУС
    boolean existsByMilitaryAccountingSpecialty(MilitaryAccountingSpecialtyEntity vus);

    // Поиск всех студентов по ВУС
    List<Student> findByMilitaryAccountingSpecialty(MilitaryAccountingSpecialtyEntity vus);

    // Поиск студентов в учебных группах
    List<Student> findByGroupStudentIn(List<StudyGroup> groups);

    // Проверка наличия студентов в учебных группах
    boolean existsByGroupStudentIn(List<StudyGroup> groups);
//    boolean existsByGroupStudent_Specialty(SpecialtyCodeInstitute specialty);
//    List<Student> findByGroupStudent_Specialty(SpecialtyCodeInstitute specialty);

    // Проверка наличия студентов в учебной группе
    boolean existsByGroupStudent(StudyGroup group);

    // Поиск студентов по учебной группе
    List<Student> findByGroupStudent(StudyGroup group);

    // Проверка наличия студентов в военном комиссариате
    boolean existsByMilitaryCommissariat(MilitaryCommissariatEntity mc);

    // Поиск студентов по венному комиссариату
    List<Student> findByMilitaryCommissariat(MilitaryCommissariatEntity mc);

    // Поиск всех студентов + сортировка (сначала алфавит групп, потом алфавит фамилий)
    List<Student> findAllByOrderByGroupStudent_NameGroupAscLastNameAsc();

//    List<Student> findByStatusAndApplicationYear(StudentStatus status, Integer applicationYear);

    // Поиск студентов по году подачи заявления
    List<Student> findAllByApplicationYear(int applicationYear);

//    List<Student> findByCourseAndGroupStudent_Specialty_IdAndPlatoon_NamePlatoon(
//            Integer course,
//            Long specialtyId,
//            String namePlatoon
//    );
//    boolean existsByPlatoon(Platoon platoon);

    // Поиск судентов по Взводу
    List<Student> findByPlatoon(Platoon platoon);




//    /**
//     * Поиск с фильтрами по:
//     *  - названию группы (contains, ignore case)
//     *  - курсу (если передан > 0, иначе игнор)
//     *  - названию специальности (contains, ignore case)
//     *  - институту (contains, ignore case)
//     *  - коду ВУС (contains, ignore case)
//     *
//     * Все фильтры опциональны: если параметр пустой / null, он не используется.
//     */
//    @Query("""
//       SELECT s FROM student s
//       LEFT JOIN s.groupStudent g
//       LEFT JOIN g.specialty sp
//       LEFT JOIN s.militaryAccountingSpecialty vus
//       WHERE (:groupName IS NULL OR :groupName = '' OR LOWER(g.nameGroup) LIKE LOWER(CONCAT('%', :groupName, '%')))
//         AND (:course IS NULL OR :course = 0 OR s.course = :course)
//         AND (:specialtyTitle IS NULL OR :specialtyTitle = '' OR LOWER(sp.titleSpecialty) LIKE LOWER(CONCAT('%', :specialtyTitle, '%')))
//         AND (:institute IS NULL OR :institute = '' OR LOWER(sp.institute) LIKE LOWER(CONCAT('%', :institute, '%')))
//         AND (:vusCode IS NULL OR :vusCode = '' OR LOWER(vus.code) LIKE LOWER(CONCAT('%', :vusCode, '%')))
//       """)
//    List<Student> findByFilters(String groupName,
//                                Integer course,
//                                String specialtyTitle,
//                                String institute,
//                                String vusCode,
//                                Sort sort);
//
//    @Query("""
//           select s
//           from student s
//           where
//               (:specialtyCode is null or :specialtyCode = ''
//                   or s.groupStudent.specialty.codeSpecialty = :specialtyCode)
//           and (:institute is null or :institute = ''
//                   or s.groupStudent.specialty.institute = :institute)
//           and (:course is null or s.course = :course)
//           and (:groupName is null or :groupName = ''
//                   or s.groupStudent.nameGroup = :groupName)
//           """)
//    List<Student> findByCompetitionFilters(
//            @Param("specialtyCode") String specialtyCode,
//            @Param("institute") String institute,
//            @Param("course") Integer course,
//            @Param("groupName") String groupName,
//            Sort sort
//    );

    // Фильтр студентов
    // Если параметр не передан, то проигнорировать
    // Если параметр передан, то фильтровать
    // Находит с льготами "with" и без льготы "without"
    @Query("""
       select s
       from student s
       where
           (:specialtyCode is null or :specialtyCode = '' 
               or s.groupStudent.specialty.codeSpecialty = :specialtyCode)
       and (:institute is null or :institute = '' 
               or s.groupStudent.specialty.institute = :institute)
       and (:course is null or s.course = :course)
       and (:groupName is null or :groupName = '' 
               or s.groupStudent.nameGroup = :groupName)
       and (
               :hasNote is null or :hasNote = ''
               or (:hasNote = 'with' 
                   and s.noteStudent is not null and trim(s.noteStudent) <> '')
               or (:hasNote = 'without' 
                   and (s.noteStudent is null or trim(s.noteStudent) = ''))
           )
       """)
    List<Student> findByCompetitionFilters(
            @Param("specialtyCode") String specialtyCode,
            @Param("institute") String institute,
            @Param("course") Integer course,
            @Param("groupName") String groupName,
            @Param("hasNote") String hasNote,
            Sort sort
    );

    // Фильтр студентов
    // ФИльтр по специальности отключается, если id = null
    // Фильтр по взводу отключается, если id = null
    // Сортировка по алфавиту фамилии и имени
    @Query("""
    SELECT s FROM student s 
    WHERE (:specialtyId IS NULL OR 
           s.platoon.specialty.id = :specialtyId)
    AND (:platoonId IS NULL OR s.platoon.id = :platoonId)
    ORDER BY s.lastName, s.firstName
""")
    List<Student> filterStudents(@Param("specialtyId") Long specialtyId, @Param("platoonId") Long platoonId);

    // Берет уникальные года поступления
    // Сортирует года от новых к старым
    @Query("SELECT DISTINCT s.applicationYear FROM student s WHERE s.applicationYear IS NOT NULL ORDER BY s.applicationYear DESC")
    List<Integer> findDistinctApplicationYears();

//    @Query("SELECT DISTINCT s.status FROM student s WHERE s.status IS NOT NULL ORDER BY s.status")
//    List<StudentStatus> findDistinctStatuses();


    // Проверка блокировки протокола распределения для указанного года подачи заявления
    @Query("SELECT COUNT(s) > 0 FROM student s WHERE s.applicationYear = :year " +
            "AND (s.platoon IS NOT NULL OR s.status IN :statuses)")
    boolean isProtocolLockedForYear(@Param("year") Integer year,
                                    @Param("statuses") List<StudentStatus> statuses);

    // Проверка блокировки протокола распределения для конкретной специальности для указанного года подачи заявления
    @Query("SELECT COUNT(s) > 0 FROM student s WHERE s.applicationYear = :year AND s.groupStudent.specialty.codeSpecialty = :code " +
            "AND (s.platoon IS NOT NULL OR s.status IN :statuses)")
    boolean isProtocolLockedForSpecialty(@Param("year") Integer year,
                                         @Param("code") String specialtyCode,
                                         @Param("statuses") List<StudentStatus> statuses);

}
