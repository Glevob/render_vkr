package ru.accouting.student.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.accouting.student.dto.ContestProtocolRowDto;
import ru.accouting.student.model.PhysicalTraining;
import ru.accouting.student.model.Student;
import ru.accouting.student.model.StudentStatus;
import ru.accouting.student.repository.StudentRepository;
import ru.accouting.student.repository.StudentSpecifications;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestProtocolService {

    private final StudentRepository studentRepository;

    // Получить все строки по году (для общего конкурса)
    public List<ContestProtocolRowDto> getAllRowsByYear(int year) {
        var spec = StudentSpecifications.withFilters(
                year,   // Год подачи заявления
                null,   // Учебная группа
                null,   // Курс обучения
                null,   // Специальность в ВУЗе
                null,   // Институт
                null,   // ВУС
                null,   // Взвод
                null,   // Статус в системе
                null    // Поиск
        );

        return studentRepository.findAll(spec).stream()
                .map(this::toRowDto)
                .toList();
    }

//    public List<ContestProtocolRowDto> getAllRowsByYearAndSpecialty(int year, String specialtyCode) {
//        var spec = StudentSpecifications.withFilters(
//                year,           // year
//                null,           // group
//                null,           // course
//                specialtyCode,  // specialty (теперь аргумент на 4-м месте)
//                null,           // institute
//                null,           // vus
//                null,           // platoonId
//                null,           // status
//                null            // search
//        );
//
//        return studentRepository.findAll(spec).stream()
//                .map(this::toRowDto)
//                .toList();
//    }

    // Получить строки только текущего пользователя
    public List<ContestProtocolRowDto> getRowsByYearForCurrentUser(int year) {
        String username = getCurrentUsername();
        if (username == null || username.isBlank()) {
            return List.of();
        }

        // Передаем 9 аргументов, заполнив недостающие значения null
        var spec = StudentSpecifications.withFilters(
                year,   // Год подачи заявления
                null,   // Учебная группа
                null,   // Курс обучения
                null,   // Специальность в ВУЗе
                null,   // Институт
                null,   // ВУС
                null,   // Взвод
                null,   // Статус в системе
                null    // Поиск
        );

        return studentRepository.findAll(spec).stream()
                .filter(student -> isStudentOfCurrentUser(student, username))
                .map(this::toRowDto)
                .toList();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    private boolean isStudentOfCurrentUser(Student student, String username) {
        if (student == null) {
            return false;
        }

        if (student.getUser() != null) {
            if (student.getUser().getUsername() != null && student.getUser().getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }

    // Допуск к конкурсу: A/B + ппо I-III + ФП "да"
    public boolean isEligible(ContestProtocolRowDto row) {
        return isPrelimPassed(row) && "да".equalsIgnoreCase(row.getPhysicalRequirementsMatch());
    }

    // Предварительный отбор: A/B + ппо I-III
    public boolean isPrelimPassed(ContestProtocolRowDto row) {
        String fit = row.getFitnessCategory();
        String psycho = row.getPsychoCategory();
        boolean isAB = "A".equalsIgnoreCase(fit) || "Б".equalsIgnoreCase(fit) || "B".equalsIgnoreCase(fit);
        boolean psychoOk = "I".equals(psycho) || "II".equals(psycho) || "III".equals(psycho);
        return isAB && psychoOk;
    }

    // Приоритет для сортировки (0,1,2)
    public int getPriorityIndex(ContestProtocolRowDto row) {
        String fit = row.getFitnessCategory();
        String psycho = row.getPsychoCategory();

        boolean isAB = "A".equalsIgnoreCase(fit) || "Б".equalsIgnoreCase(fit) || "B".equalsIgnoreCase(fit);
        if (!isAB) {
            return 2;
        }

        if ("I".equals(psycho) || "II".equals(psycho)) {
            return 0;
        }

        if ("III".equals(psycho)) {
            return 1;
        }

        return 2;
    }

    // Причины отказа
    public String generateFailedReasons(ContestProtocolRowDto row) {
        List<String> reasons = new ArrayList<>();

        if ("IV".equalsIgnoreCase(row.getPsychoCategory())) {
            reasons.add("IV группа ППО");
        }

        String fit = row.getFitnessCategory();
        if (!("A".equalsIgnoreCase(fit) || "Б".equalsIgnoreCase(fit) || "B".equalsIgnoreCase(fit))) {
            reasons.add("Категория годности " + fit);
        }

        if ("нет".equalsIgnoreCase(row.getPhysicalRequirementsMatch())) {
            reasons.add("Не соответствует уровню ФП");
        }

        if (row.getTotalPoints() == null || row.getTotalPoints() == 0) {
            reasons.add("Не представил результаты ФП");
        }

        return reasons.isEmpty() ? "Не прошёл конкурс" : String.join(". ", reasons);
    }

    @Transactional
    public void updateContestStatuses(int year, int limit) {
        List<ContestProtocolRowDto> allStudents = getAllRowsByYear(year);

        List<ContestProtocolRowDto> allEligible = allStudents.stream()
                .filter(this::isEligible)
                .sorted(Comparator
                        .comparingInt(this::getPriorityIndex)
                        .thenComparing(ContestProtocolRowDto::getFinalScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int effectiveLimit = Math.min(limit, allEligible.size());

        Set<Long> candidateIds = allEligible.stream()
                .limit(effectiveLimit)
                .map(ContestProtocolRowDto::getStudentId)
                .collect(Collectors.toSet());

        Set<Long> rejectedIds = allEligible.stream()
                .skip(effectiveLimit)
                .map(ContestProtocolRowDto::getStudentId)
                .collect(Collectors.toSet());

        var students = studentRepository.findAll(
                StudentSpecifications.withFilters(
                        year,   // Год подачи заявления
                        null,   // Учебная группа
                        null,   // Курс обучения
                        null,   // Специальность в ВУЗе
                        null,   // Институт
                        null,   // ВУС
                        null,   // Взвод
                        null,   // Статус в системе
                        null    // Поиск
                )
        );

        for (Student student : students) {
            if (student.getIdStudent() != null) {
                Long id = student.getIdStudent();
                if (candidateIds.contains(id)) {
                    student.setStatus(StudentStatus.CANDIDATE);
                } else if (rejectedIds.contains(id)) {
                    student.setStatus(StudentStatus.REJECTED);
                } else {
                    student.setStatus(StudentStatus.NOT_PASSED);
                }
            }
        }

        studentRepository.saveAll(students);
    }

    // Полный маппинг Student → DTO
    public ContestProtocolRowDto toRowDto(Student s) {
        ContestProtocolRowDto dto = new ContestProtocolRowDto();

        dto.setStudentId(s.getIdStudent());

        StringBuilder fullName = new StringBuilder();
        if (s.getLastName() != null) fullName.append(s.getLastName()).append(" ");
        if (s.getFirstName() != null) fullName.append(s.getFirstName()).append(" ");
        if (s.getPatronymic() != null) fullName.append(s.getPatronymic());
        dto.setFullName(fullName.toString().trim());

        if (s.getBirthday() != null) {
            dto.setBirthDate(s.getBirthday().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }

        // 1. Код программы подготовки (для логики/группировки)
        if (s.getMilitaryAccountingSpecialty() != null) {
            dto.setProgramCode(s.getMilitaryAccountingSpecialty().getCode());
        }

        // 2. Код специальности ВУЗа (для выгрузки в Excel)
        if (s.getGroupStudent() != null && s.getGroupStudent().getSpecialty() != null) {
            dto.setUniversitySpecialtyCode(s.getGroupStudent().getSpecialty().getCodeSpecialty());
        } else {
            dto.setUniversitySpecialtyCode("—");
        }

        if (s.getFitnessCategory() != null) {
            dto.setFitnessCategory(s.getFitnessCategory().name());
        }
        if (s.getPsychoCategory() != null) {
            dto.setPsychoCategory(s.getPsychoCategory().name());
        }

        if (s.getAge() != null) {
            dto.setAgeGroup(s.isUnder25() ? "до 25" : "25 и старше");
        }

        PhysicalTraining pt = s.getPhysicalTraining();
        if (pt != null) {
            dto.setStrengthExerciseNumber(pt.getStrengthExerciseNumber());
            dto.setStrengthResult(pt.getStrengthResult() != null ? pt.getStrengthResult().toString() : null);
            dto.setStrengthPoints(pt.getStrengthPoints());

            dto.setSpeedExerciseNumber(pt.getSpeedExerciseNumber());
            dto.setSpeedResult(pt.getSpeedResult() != null ? String.format("%.2f", pt.getSpeedResult()) : null);
            dto.setSpeedPoints(pt.getSpeedPoints());

            dto.setEnduranceExerciseNumber(pt.getEnduranceExerciseNumber());
            dto.setEnduranceResult(pt.getEnduranceResult() != null ? String.format("%.2f", pt.getEnduranceResult()) : null);
            dto.setEndurancePoints(pt.getEndurancePoints());

            dto.setTotalPoints(pt.getTotalPoints());
            dto.setPhysical100(pt.getFinalResult());
        } else {
            dto.setTotalPoints(0);
            dto.setPhysical100(null);
        }

        boolean hasNotPassedExercise = (dto.getStrengthPoints() != null && dto.getStrengthPoints() == 0) ||
                (dto.getSpeedPoints() != null && dto.getSpeedPoints() == 0) ||
                (dto.getEndurancePoints() != null && dto.getEndurancePoints() == 0);

        int totalPhysical = 0;
        if (dto.getStrengthPoints() != null) totalPhysical += dto.getStrengthPoints();
        if (dto.getSpeedPoints() != null) totalPhysical += dto.getSpeedPoints();
        if (dto.getEndurancePoints() != null) totalPhysical += dto.getEndurancePoints();

        int threshold = s.isUnder25() ? 120 : 110;
        dto.setPhysicalRequirementsMatch((hasNotPassedExercise || totalPhysical < threshold) ? "нет" : "да");

        if (s.getGrade100() != null) {
            dto.setAcademic100(s.getGrade100().intValue());
        }

        int finalScore = 0;
        if (dto.getPhysical100() != null) finalScore += dto.getPhysical100();
        if (dto.getAcademic100() != null) finalScore += dto.getAcademic100();
        dto.setFinalScore(finalScore);

        dto.setHasQuotaRight(false);
        dto.setHasPriorityRight(false);
        dto.setCommissionDecision(null);

        if (s.getStatus() != null) {
            dto.setStatus(s.getStatus());
        }

        if (s.getPlatoon() != null) {
            dto.setPlatoonId(s.getPlatoon().getId());
            dto.setPlatoonName(s.getPlatoon().getNamePlatoon());
        } else {
            dto.setPlatoonId(null);
            dto.setPlatoonName(null);
        }

        return dto;
    }

    public List<Integer> getAvailableYears() {
        return studentRepository.findAll().stream()
                .map(Student::getApplicationYear)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    public boolean isProtocolLocked(Integer year, String specialtyCode) {
        List<StudentStatus> targetStatuses = List.of(
                StudentStatus.CADET,
                StudentStatus.RESERVE,
                StudentStatus.GRADUATION
        );

        if (specialtyCode == null || specialtyCode.isBlank()) {
            return studentRepository.isProtocolLockedForYear(year, targetStatuses);
        } else {
            return studentRepository.isProtocolLockedForSpecialty(year, specialtyCode, targetStatuses);
        }
    }
}