package ru.accouting.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.accouting.student.dto.ContestProtocolRowDto;
import ru.accouting.student.model.*;
import ru.accouting.student.repository.MilitaryAccountingSpecialtyEntityRepository;
import ru.accouting.student.repository.PlatoonRepository;
import ru.accouting.student.repository.StudentRepository;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentManagementService {
    private final StudentRepository studentRepository;
    private final MilitaryAccountingSpecialtyEntityRepository specialtyRepository;
    private final PlatoonRepository platoonRepository;
    private final ContestProtocolService contestProtocolService;

//    public List<ContestProtocolRowDto> getAllStudentsDtoByYear(int year) {
//        return studentRepository.findAllByApplicationYear(year).stream()
//                .map(contestProtocolService::toRowDto)
//                .sorted(Comparator.comparingInt(this::getPriorityIndex)
//                        .thenComparing(Comparator.comparing(
//                                ContestProtocolRowDto::getFinalScore,
//                                Comparator.nullsLast(Comparator.naturalOrder())
//                        ).reversed()))
//                .collect(Collectors.toList());
//    }

    private int getPriorityIndex(ContestProtocolRowDto r) {
        String fit = r.getFitnessCategory();
        String psycho = r.getPsychoCategory();
        boolean isAB = "A".equalsIgnoreCase(fit) || "Б".equalsIgnoreCase(fit) || "B".equalsIgnoreCase(fit);

        if (!isAB) return 2;
        if ("I".equals(psycho) || "II".equals(psycho)) return 0;
        if ("III".equals(psycho)) return 1;
        return 2;
    }

    public List<MilitaryAccountingSpecialtyEntity> getAllSpecialties() {
        return specialtyRepository.findAll();
    }
    public List<Platoon> getAllPlatoons() {return platoonRepository.findAll();}

    @Transactional
    public void updateStudentDetails(Long studentId, StudentStatus status, Long platoonId) {
        Student s = studentRepository.findById(studentId).orElseThrow();
        if (platoonId != null && s.getStatus() == StudentStatus.CANDIDATE) {
            s.setStatus(StudentStatus.CADET);
        } else {
            // В остальных случаях оставляем статус, который пришел из формы/контроллера
            s.setStatus(status);
        }

        if (platoonId == null) {
            s.setPlatoon(null);
        } else {
            Platoon platoon = platoonRepository.findById(platoonId).orElseThrow();
            s.setPlatoon(platoon);
        }

        studentRepository.save(s);
    }

    // Получение уникальных годов подачи заявления
    public List<Integer> getAvailableYears() {
        return studentRepository.findAll().stream()
                .map(Student::getApplicationYear)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

//    public List<ContestProtocolRowDto> getCandidatesDtoByYear(int year) {
//        return studentRepository.findAllByApplicationYear(year).stream()
//                .filter(s -> s.getStatus() == StudentStatus.CANDIDATE || s.getStatus() == StudentStatus.CADET)  // ✅ Добавлены Кадеты
//                .map(contestProtocolService::toRowDto)
//                .sorted(Comparator.comparingInt(this::getPriorityIndex)
//                        .thenComparing(Comparator.comparing(
//                                ContestProtocolRowDto::getFinalScore,
//                                Comparator.nullsLast(Comparator.naturalOrder())
//                        ).reversed()))
//                .collect(Collectors.toList());
//    }

    // Формирование списка поступивших студентов
    public List<ContestProtocolRowDto> getCandidatesDtoByYearAndSpecialty(int year, Long specialtyId) {
        return studentRepository.findAllByApplicationYear(year).stream()
                .filter(s -> s.getStatus() == StudentStatus.CANDIDATE || s.getStatus() == StudentStatus.CADET)  // ✅ Добавлены Кадеты
                .filter(s -> specialtyId == null ||
                        (s.getMilitaryAccountingSpecialty() != null &&
                                specialtyId.equals(s.getMilitaryAccountingSpecialty().getId())))
                .map(contestProtocolService::toRowDto)
                .sorted(Comparator.comparingInt(this::getPriorityIndex)
                        .thenComparing(Comparator.comparing(
                                ContestProtocolRowDto::getFinalScore,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ).reversed()))
                .collect(Collectors.toList());
    }

    // Распределение Кандидатов по взводам
    @Transactional
    public void autoAssignPlatoonsBySpecialty(int year, int maxPerPlatoon) {
        if (maxPerPlatoon <= 0) {
            return;
        }

        // Получаем только кандидатов
        List<Student> candidates = studentRepository.findAllByApplicationYear(year).stream()
                .filter(s -> s.getStatus() == StudentStatus.CANDIDATE)
                .filter(s -> s.getMilitaryAccountingSpecialty() != null)
                .sorted(Comparator
                        .comparingInt((Student s) -> getPriorityIndex(contestProtocolService.toRowDto(s)))
                        .thenComparing((Student s) -> {
                            ContestProtocolRowDto dto = contestProtocolService.toRowDto(s);
                            return dto.getFinalScore() == null ? 0 : dto.getFinalScore();
                        }, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // Фильтр взводов: только с первой цифрой "1" (101, 102, но не 201)
        List<Platoon> eligiblePlatoons = platoonRepository.findAll().stream()
                .filter(p -> p.getNamePlatoon() != null)
                .filter(p -> String.valueOf(p.getNamePlatoon()).startsWith("1"))
                .collect(Collectors.toList());

        if (eligiblePlatoons.isEmpty() || candidates.isEmpty()) {
            return;
        }

        // Счетчики для каждого взвода
        java.util.Map<Long, Integer> platoonCounts = new java.util.HashMap<>();
        for (Platoon platoon : eligiblePlatoons) {
            platoonCounts.put(platoon.getId(), 0);
        }

        List<Student> assignedStudents = new java.util.ArrayList<>();

        // 1. Сначала студенты с конкретной программой (НЕ ALL)
        List<Student> specificProgramStudents = candidates.stream()
                .filter(s -> !"ALL".equalsIgnoreCase(s.getMilitaryAccountingSpecialty().getCode()))
                .collect(Collectors.toList());

        for (Student student : specificProgramStudents) {
            Long specialtyId = student.getMilitaryAccountingSpecialty().getId();

            // Ищем подходящий взвод этой программы среди взводов с "1"
            for (Platoon platoon : eligiblePlatoons) {
                if (platoon.getSpecialty() != null
                        && specialtyId.equals(platoon.getSpecialty().getId())
                        && platoonCounts.get(platoon.getId()) < maxPerPlatoon) {

                    student.setPlatoon(platoon);
                    student.setStatus(StudentStatus.CADET);
                    platoonCounts.put(platoon.getId(), platoonCounts.get(platoon.getId()) + 1);
                    assignedStudents.add(student);
                    break;
                }
            }
        }

        // 2. Потом студенты с программой "ALL" — заполняют оставшиеся места
        List<Student> allProgramStudents = candidates.stream()
                .filter(s -> "ALL".equalsIgnoreCase(s.getMilitaryAccountingSpecialty().getCode()))
                .collect(Collectors.toList());

        for (Student student : allProgramStudents) {
            // Ищем любой взвод с "1" и свободным местом (по порядку)
            for (Platoon platoon : eligiblePlatoons) {
                if (platoonCounts.get(platoon.getId()) < maxPerPlatoon) {
                    student.setPlatoon(platoon);
                    student.setStatus(StudentStatus.CADET);
                    platoonCounts.put(platoon.getId(), platoonCounts.get(platoon.getId()) + 1);
                    assignedStudents.add(student);
                    break;
                }
            }
        }

        // Сохраняем всех назначенных
        studentRepository.saveAll(assignedStudents);
    }

//    public List<ContestProtocolRowDto> getRejectedDtoByYear(int year) {
//        return studentRepository.findAllByApplicationYear(year).stream()
//                .filter(s -> s.getStatus() == StudentStatus.REJECTED)
//                .map(contestProtocolService::toRowDto)
//                .sorted(Comparator.comparingInt(this::getPriorityIndex)
//                        .thenComparing(Comparator.comparing(
//                                ContestProtocolRowDto::getFinalScore,
//                                Comparator.nullsLast(Comparator.naturalOrder())
//                        ).reversed()))
//                .collect(Collectors.toList());
//    }

    // Формирует список отклоненных студентов а определенный год
    public List<ContestProtocolRowDto> getRejectedDtoByYearAndSpecialty(int year, Long specialtyId) {
        return studentRepository.findAllByApplicationYear(year).stream()
                .filter(s -> s.getStatus() == StudentStatus.REJECTED)
                .filter(s -> specialtyId == null ||
                        (s.getMilitaryAccountingSpecialty() != null &&
                                specialtyId.equals(s.getMilitaryAccountingSpecialty().getId())))
                .map(contestProtocolService::toRowDto)
                .sorted(Comparator.comparingInt(this::getPriorityIndex)
                        .thenComparing(Comparator.comparing(
                                ContestProtocolRowDto::getFinalScore,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ).reversed()))
                .collect(Collectors.toList());
    }


     //Проверяет, есть ли хотя бы один студент, который уже распределен в любой взвод 1-го курса
    public boolean hasStudentsInFirstYearPlatoons() {
        return studentRepository.findAll().stream()
                .filter(s -> s.getPlatoon() != null && s.getPlatoon().getNamePlatoon() != null)
                .anyMatch(s -> String.valueOf(s.getPlatoon().getNamePlatoon()).startsWith("1"));
    }

}
