package ru.accouting.student.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.accouting.student.dto.StudentCredentialsRow;
import ru.accouting.student.model.*;
import ru.accouting.student.repository.*;
import ru.accouting.student.repository.StudentSpecifications;
import org.springframework.data.jpa.domain.Specification;
import ru.accouting.student.service.CreatedStudentResponse;
import ru.accouting.student.service.HashService;
import ru.accouting.student.service.StudentService;
import ru.accouting.student.service.StudentStatusService;
import ru.accouting.student.util.ExcelExportUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository;
    private final PlatoonRepository platoonRepository;
    private final SpecialtyCodeInstituteRepository specialtyCodeInstituteRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final MilitaryCommissariatRepository militaryCommissariatRepository;
    private final MilitaryAccountingSpecialtyEntityRepository militaryAccountingSpecialtyEntityRepository;
    private final StudentStatusService studentStatusService;
    private final StudentService studentService;
    private final StudentCredentialsRepository studentCredentialsRepository;
    private final HashService hashService;
    private final PassportRepository passportRepository;


    // Подавшие заявление////////////////////////////////////////////////////////////////////
    // Список студентов + сортировка + фильтр
    @GetMapping("/student-applied")
    public String studentAppliedList(
            @RequestParam(name = "sortField", required = false, defaultValue = "lastName") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "asc") String sortDir,
            @RequestParam(name = "applicationYear", required = false) Integer applicationYear,
            @RequestParam(name = "group", required = false) String groupName,
            @RequestParam(name = "course", required = false) Integer course,
            @RequestParam(name = "specialty", required = false) String specialtyTitle,
            @RequestParam(name = "institute", required = false) String institute,
            @RequestParam(name = "vus", required = false) String vusCode,
            @RequestParam(name = "platoonId", required = false) Long platoonId,
            @RequestParam(name = "search", required = false) String search,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Specification<Student> spec = StudentSpecifications.withFilters(
                applicationYear,
                groupName,
                course,
                specialtyTitle,
                institute,
                vusCode,
                platoonId,
                StudentStatus.APPLIED,
                search
        );

        List<Student> students = studentRepository.findAll(spec, sort);

        List<Integer> applicationYears = studentRepository.findDistinctApplicationYears();
        model.addAttribute("applicationYears", applicationYears);

        model.addAttribute("students", students);

        // Данные для фильтров
        model.addAttribute("groups", studyGroupRepository.findAll(Sort.by("nameGroup")));
        model.addAttribute("courses", List.of(1, 2));
        model.addAttribute("specialties", specialtyCodeInstituteRepository.findAll(Sort.by("codeSpecialty")));
        model.addAttribute("institutes", specialtyCodeInstituteRepository.findDistinctInstitute());
        model.addAttribute("vusList", militaryAccountingSpecialtyEntityRepository.findAll(Sort.by("code")));

        // Передаем параметры обратно в модель для отображения в селектах
        model.addAttribute("selectedApplicationYear", applicationYear);
        model.addAttribute("selectedGroup", groupName);
        model.addAttribute("selectedCourse", course);
        model.addAttribute("selectedSpecialty", specialtyTitle);
        model.addAttribute("selectedInstitute", institute);
        model.addAttribute("selectedVus", vusCode);
        model.addAttribute("selectedPlatoonId", platoonId);
        model.addAttribute("search", search);

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "student-main";
    }

    // Форма добавления студента
    @GetMapping("/student-applied/add")
    public String studentAddForm(Model model) {
        Student student = new Student();
        student.setPassport(new Passport());
        student.setStatus(StudentStatus.APPLIED);

        model.addAttribute("student", student);

        model.addAttribute("groups",
                studyGroupRepository.findAll(Sort.by("nameGroup")));
        model.addAttribute("vusList",
                militaryAccountingSpecialtyEntityRepository.findAll(Sort.by("code")));
        model.addAttribute("commissariats",
                militaryCommissariatRepository.findAll(Sort.by("name")));

        return "student-add";
    }

    // Добавление студента////////////////////////////////////////////////////////////
    @Transactional
    @PostMapping("/student-applied/add")
    public String addStudent(@ModelAttribute("student") Student student,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        // Регулярные выражения для валидации
        String lettersRegex = "^[a-zA-Zа-яА-ЯёЁ\\s-]+$";
        String studentIdRegex = "^[a-zA-Zа-яА-ЯёЁ0-9-]{1,10}$"; // Буквы, цифры, дефис — макс. 10 символов
        String phoneRegex = "^[0-9]{11}$";                      // 11 цифр
        String passportSeriesRegex = "^[0-9]{4}$";              // 4 цифры
        String passportNumberRegex = "^[0-9]{6}$";              // 6 цифр

        // Валидация ФИО
        if (student.getLastName() == null || student.getLastName().isBlank() || !student.getLastName().trim().matches(lettersRegex)) {
            bindingResult.rejectValue("lastName", "error.student", "Фамилия обязательна и должна содержать только буквы");
        }
        if (student.getFirstName() == null || student.getFirstName().isBlank() || !student.getFirstName().trim().matches(lettersRegex)) {
            bindingResult.rejectValue("firstName", "error.student", "Имя обязательно и должно содержать только буквы");
        }
        if (student.getPatronymic() != null && !student.getPatronymic().isBlank() && !student.getPatronymic().trim().matches(lettersRegex)) {
            bindingResult.rejectValue("middleName", "error.student", "Отчество должно содержать только буквы");
        }

        // Валидация учебной группы
        if (student.getGroupStudent() == null) {
            bindingResult.rejectValue("groupStudent", "error.student", "Пожалуйста, выберите учебную группу");
        }

        // Валидация студенческого билета
        String idCard = student.getStudentIdCard() != null ? student.getStudentIdCard().trim() : "";

        if (idCard.isBlank() || !idCard.matches(studentIdRegex)) {
            bindingResult.rejectValue("studentIdCard", "error.student",
                    "Студенческий билет должен содержать буквы/цифры и быть не длиннее 10 символов");
        } else {
            // Проверка уникальности
            if (studentRepository.existsByStudentIdCard(idCard)) {
                bindingResult.rejectValue("studentIdCard", "error.student",
                        "Студент с таким номером билета уже существует в системе");
            }
        }

        // Валидация номера телефона
        if (student.getPhoneNumber() == null || !student.getPhoneNumber().trim().matches(phoneRegex)) {
            bindingResult.rejectValue("phoneNumber", "error.student", "Номер должен состоять ровно из 11 цифр");
        } else if (studentRepository.existsByPhoneNumber(student.getPhoneNumber().trim())) {
            bindingResult.rejectValue("phoneNumber", "error.student", "Студент с таким телефоном уже существует");
        }

        // Валидация паспортных данных
        Passport p = student.getPassport();
        if (p == null) {
            bindingResult.rejectValue("passport.seriesPassport", "error.student", "Паспортные данные обязательны");
        } else {
            boolean isFormatValid = true;
            if (p.getSeriesPassport() == null || !p.getSeriesPassport().trim().matches(passportSeriesRegex)) {
                bindingResult.rejectValue("passport.seriesPassport", "error.student", "Серия: 4 цифры");
                isFormatValid = false;
            }
            if (p.getNumberPassport() == null || !p.getNumberPassport().trim().matches(passportNumberRegex)) {
                bindingResult.rejectValue("passport.numberPassport", "error.student", "Номер: 6 цифр");
                isFormatValid = false;
            }

            // Проверка уникальности через хеш
            if (isFormatValid) {
                String hash = hashService.generatePassportHash(p.getSeriesPassport().trim(), p.getNumberPassport().trim());
                if (passportRepository.existsByPassportHash(hash)) {
                    bindingResult.rejectValue("passport.seriesPassport", "error.student", "Этот паспорт уже зарегистрирован");
                    bindingResult.rejectValue("passport.numberPassport", "error.student", "Этот паспорт уже зарегистрирован");
                } else {
                    // Если всё ок, записываем хеш в сущность, чтобы сохранить в БД
                    p.setPassportHash(hash);
                }
            }
        }

        // Выход при ошибках
        if (bindingResult.hasErrors()) {
            // Заново наполняем списки для выпадающих меню
            model.addAttribute("groups", studyGroupRepository.findAll(Sort.by("nameGroup")));
            model.addAttribute("vusList", militaryAccountingSpecialtyEntityRepository.findAll(Sort.by("code")));
            model.addAttribute("commissariats", militaryCommissariatRepository.findAll(Sort.by("name")));
            return "student-add";
        }

        if (student.getGroupStudent() != null) {
            student.setCourse(student.getGroupStudent().getCourse());
        }

        if (student.getPassport() != null) {
            student.getPassport().setStudent(student);
        }

        student.setStatus(StudentStatus.APPLIED);

        student.setApplicationYear(LocalDate.now().getYear());

        CreatedStudentResponse result = studentService.addStudent(student);

        // Передаем логин и пароль на форму подтверждения
        redirectAttributes.addFlashAttribute("createdLogin", result.login());
        redirectAttributes.addFlashAttribute("createdPassword", result.rawPassword());

        return "redirect:/student-applied";
    }

    @GetMapping("/student-applied/export-credentials")
    public void exportCredentials(
            @RequestParam(name = "applicationYear", required = false) Integer applicationYear,
            @RequestParam(name = "group", required = false) String groupName,
            @RequestParam(name = "course", required = false) Integer course,
            @RequestParam(name = "specialty", required = false) String specialtyTitle,
            @RequestParam(name = "institute", required = false) String institute,
            @RequestParam(name = "sortField", required = false, defaultValue = "lastName") String sortField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "asc") String sortDir,
            HttpServletResponse response) throws IOException {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        // Передаем все параметры, передавая null для неиспользуемых в экспорте
        // Статус APPLIED теперь передается прямо в метод
        Specification<Student> spec = StudentSpecifications.withFilters(
                applicationYear,
                groupName,
                course,
                specialtyTitle,
                institute,
                null,
                null,
                StudentStatus.APPLIED,
                null
        );

        List<Student> students = studentRepository.findAll(spec, sort);
        List<StudentCredentialsRow> rows = studentService.getCredentialsRowsAndDelete(students);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=student-credentials.xlsx");

        ExcelExportUtil.exportCredentials(rows, response.getOutputStream());
    }

    // Детальная страница студента
    @GetMapping("/student-applied/{id}")
    public String studentDetails(@PathVariable("id") long id, Model model) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный идентификатор студента: " + id));
        model.addAttribute("student", student);
        return "student-details";
    }

    // Редактирование////////////////////////////////////////////////////////////////////////////
    @GetMapping("/student-applied/{id}/edit")
    public String studentEdit(@PathVariable("id") long idStudent, Model model) {
        Student student = studentRepository.findById(idStudent).orElseThrow();

        if (student.getPassport() == null) {
            student.setPassport(new Passport());
        }

        model.addAttribute("student", student);
        model.addAttribute("groups",
                studyGroupRepository.findAll(Sort.by("nameGroup")));
        model.addAttribute("vusList",
                militaryAccountingSpecialtyEntityRepository.findAll(Sort.by("code")));
        model.addAttribute("commissariats",
                militaryCommissariatRepository.findAll(Sort.by("name")));

        return "student-edit";
    }

    // Редактирование студента
    @Transactional
    @PostMapping("/student-applied/{id}/edit")
    public String studentUpdate(@PathVariable("id") long idStudent,
                                @ModelAttribute("student") Student formStudent,
                                BindingResult bindingResult,
                                Model model) {

        String lettersRegex = "^[a-zA-Zа-яА-ЯёЁ\\s-]+$";
        String studentIdRegex = "^[a-zA-Zа-яА-ЯёЁ0-9-]+$";
        String digitsRegex = "^[0-9]+$";

        // Валидация ФИО
        if (formStudent.getLastName() != null && !formStudent.getLastName().trim().matches(lettersRegex)) {
            bindingResult.rejectValue("lastName", "error.student", "Фамилия должна состоять только из букв");
        }
        if (formStudent.getFirstName() != null && !formStudent.getFirstName().trim().matches(lettersRegex)) {
            bindingResult.rejectValue("firstName", "error.student", "Имя должно состоять только из букв");
        }
        if (formStudent.getPatronymic() != null && !formStudent.getPatronymic().isBlank() && !formStudent.getPatronymic().trim().matches(lettersRegex)) {
            bindingResult.rejectValue("patronymic", "error.student", "Отчество должно состоять только из букв");
        }

        // Валидация студенческого билета
        if (formStudent.getStudentIdCard() != null && !formStudent.getStudentIdCard().isBlank()) {
            if (!formStudent.getStudentIdCard().trim().matches(studentIdRegex)) {
                bindingResult.rejectValue("studentIdCard", "error.student", "Разрешены только буквы, цифры и дефис");
            } else {
                studentRepository.findByStudentIdCard(formStudent.getStudentIdCard().trim()).ifPresent(existing -> {
                    if (!existing.getIdStudent().equals(idStudent)) {
                        bindingResult.rejectValue("studentIdCard", "error.student", "Этот билет уже принадлежит другому студенту");
                    }
                });
            }
        }

        // Валидация номера телефона
        if (formStudent.getPhoneNumber() != null && !formStudent.getPhoneNumber().isBlank()) {
            if (!formStudent.getPhoneNumber().trim().matches(digitsRegex)) {
                bindingResult.rejectValue("phoneNumber", "error.student", "Разрешены только цифры");
            } else {
                studentRepository.findByPhoneNumber(formStudent.getPhoneNumber().trim()).ifPresent(existing -> {
                    if (!existing.getIdStudent().equals(idStudent)) {
                        bindingResult.rejectValue("phoneNumber", "error.student", "Этот телефон уже закреплен за другим");
                    }
                });
            }
        }

        // Валидация паспортных данных
        if (formStudent.getPassport() != null) {
            String series = formStudent.getPassport().getSeriesPassport();
            String number = formStudent.getPassport().getNumberPassport();
            boolean isSeriesValid = true;
            boolean isNumberValid = true;

            if (series != null && !series.isBlank() && !series.trim().matches(digitsRegex)) {
                bindingResult.rejectValue("passport.seriesPassport", "error.student", "Только цифры");
                isSeriesValid = false;
            }
            if (number != null && !number.isBlank() && !number.trim().matches(digitsRegex)) {
                bindingResult.rejectValue("passport.numberPassport", "error.student", "Только цифры");
                isNumberValid = false;
            }

            if (isSeriesValid && isNumberValid && series != null && !series.isBlank() && number != null && !number.isBlank()) {
                studentRepository.findByPassport_SeriesPassportAndPassport_NumberPassport(series.trim(), number.trim()).ifPresent(existing -> {
                    if (!existing.getIdStudent().equals(idStudent)) {
                        bindingResult.rejectValue("passport.seriesPassport", "error.student", "Паспортные данные уже заняты");
                    }
                });
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("groups", studyGroupRepository.findAll(Sort.by("nameGroup")));
            model.addAttribute("vusList", militaryAccountingSpecialtyEntityRepository.findAll(Sort.by("code")));
            model.addAttribute("commissariats", militaryCommissariatRepository.findAll(Sort.by("name")));
            return "student-edit";
        }

        Student existingStudent = studentRepository.findById(idStudent).orElseThrow();
        existingStudent.setFirstName(formStudent.getFirstName());
        existingStudent.setLastName(formStudent.getLastName());
        existingStudent.setPatronymic(formStudent.getPatronymic());
        existingStudent.setBirthday(formStudent.getBirthday());
        existingStudent.setGroupStudent(formStudent.getGroupStudent());
        if (formStudent.getGroupStudent() != null) { existingStudent.setCourse(formStudent.getGroupStudent().getCourse()); } else { existingStudent.setCourse(formStudent.getCourse()); }
        existingStudent.setMilitaryCommissariat(formStudent.getMilitaryCommissariat());
        existingStudent.setMilitaryAccountingSpecialty(formStudent.getMilitaryAccountingSpecialty());
        existingStudent.setStudentIdCard(formStudent.getStudentIdCard());
        existingStudent.setPhoneNumber(formStudent.getPhoneNumber());
        existingStudent.setNoteStudent(formStudent.getNoteStudent());
        Passport formPassport = formStudent.getPassport();
        Passport existingPassport = existingStudent.getPassport();
        boolean formPassportEmpty = (formPassport == null) || ((formPassport.getNumberPassport() == null || formPassport.getNumberPassport().isBlank()) && (formPassport.getSeriesPassport() == null || formPassport.getSeriesPassport().isBlank()) && formPassport.getDatePassport() == null && (formPassport.getPlacePassport() == null || formPassport.getPlacePassport().isBlank()));
        if (formPassportEmpty) { existingStudent.setPassport(null); } else { if (existingPassport == null) { existingPassport = new Passport(); existingPassport.setStudent(existingStudent); existingStudent.setPassport(existingPassport); } existingPassport.setNumberPassport(formPassport.getNumberPassport()); existingPassport.setSeriesPassport(formPassport.getSeriesPassport()); existingPassport.setPlacePassport(formPassport.getPlacePassport()); existingPassport.setDatePassport(formPassport.getDatePassport()); }
        studentStatusService.recalculateStatus(existingStudent);
        studentRepository.save(existingStudent);
        return "redirect:/student-applied/" + idStudent;
    }

    // Удаление///////////////////////////////////////////////////////////////////////
    @Transactional
    @PostMapping("/student-applied/{id}/delete")
    public String studentDelete(@PathVariable("id") long idStudent) {
        Student student = studentRepository.findById(idStudent).orElseThrow();
        studentRepository.delete(student);
        return "redirect:/student-applied";
    }

    @PostMapping("/student-applied/{id}/approve")
    public String approveStudent(@PathVariable("id") Long id) {
        Student student = studentRepository.findById(id).orElseThrow();

        student.setStatus(StudentStatus.CADET);

        List<Platoon> platoons = platoonRepository.findByNamePlatoon(String.valueOf(student.getCourse()));
        if (platoons.isEmpty()) {
            throw new IllegalStateException("Нет взводов для курса " + student.getCourse());
        }

        Platoon targetPlatoon = platoons.get(0);
        student.setPlatoon(targetPlatoon);

        studentRepository.save(student);
        return "redirect:/student-applied/" + id;
    }

    // Физическая подготовка////////////////////////////////////////////////////////////////////
//    private String calculateFinalResult(int totalPoints) {
//        if (totalPoints >= 235) return "100";
//        if (totalPoints >= 220) return "95";
//        if (totalPoints >= 200) return "85";
//        if (totalPoints >= 180) return "75";
//        if (totalPoints >= 160) return "65";
//        return "незачёт";
//    }

    @GetMapping("/students")
    public String allStudentsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "sortField", defaultValue = "lastName") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,

            @RequestParam(required = false) Integer applicationYear,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) Integer course,
            @RequestParam(required = false) String specialtyTitle,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String vusCode,
            @RequestParam(required = false) Long platoonId,
            @RequestParam(required = false) StudentStatus status,
            @RequestParam(required = false) String search,

            Model model) {

        // Настройка сортировки
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        // Создание объекта пагинации
        Pageable pageable = PageRequest.of(page, size, sort);

        // Вызов сервиса
        Page<Student> studentPage = studentService.getFilteredStudents(
                applicationYear, groupName, course, specialtyTitle, institute, vusCode, platoonId, status, search, pageable
        );

        // Передача данных в модель
        model.addAttribute("studentPage", studentPage);
        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("totalItems", studentPage.getTotalElements());
        model.addAttribute("size", size);

        // Данные для фильтров
        model.addAttribute("applicationYears", studentRepository.findDistinctApplicationYears());
        model.addAttribute("groups", studyGroupRepository.findAll(Sort.by("nameGroup")));
        model.addAttribute("courses", List.of(1, 2, 3, 4, 5));
        model.addAttribute("institutes", specialtyCodeInstituteRepository.findDistinctInstitute());
        model.addAttribute("platoons", platoonRepository.findAll(Sort.by("namePlatoon")));
        model.addAttribute("statuses", List.of(StudentStatus.values()));

        // Сохранение выбранных значений в полях (для формы)
        model.addAttribute("selectedApplicationYear", applicationYear);
        model.addAttribute("selectedGroupName", groupName);
        model.addAttribute("selectedCourse", course);
        model.addAttribute("selectedSpecialtyTitle", specialtyTitle);
        model.addAttribute("selectedInstitute", institute);
        model.addAttribute("selectedVusCode", vusCode);
        model.addAttribute("selectedPlatoonId", platoonId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);

        // Параметры сортировки для ссылок
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "students/list";
    }


    // Перевод студентов в "Запас"///////////////////////////////////////////////////////////////////
    @Transactional
    @PostMapping("/students/transfer-to-reserve")
    public String transferGraduatesToReserve(RedirectAttributes redirectAttributes) {
        // Находим всех студентов со статусом GRADUATE (Выпуск)
        List<Student> graduates = studentRepository.findAll().stream()
                .filter(s -> s.getStatus() == StudentStatus.GRADUATION)
                .toList();

        if (graduates.isEmpty()) {
            // Если переводить некого, выводим предупреждение
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Студенты со статусом «Выпуск» не найдены. Перевод не требуется.");
            return "redirect:/students";
        }

        // Обновляем статус и очищаем взвод у каждого
        for (Student student : graduates) {
            student.setStatus(StudentStatus.RESERVE); // Меняем статус на ЗАПАС
            student.setPlatoon(null);                 // Снимаем со взвода
        }

        // Сохраняем массовые изменения
        studentRepository.saveAll(graduates);

        // Передаем сообщение об успехе в HTML-шаблон
        redirectAttributes.addFlashAttribute("message",
                "Успешно переведено в статус «Запас» курсантов: " + graduates.size());

        return "redirect:/students";
    }
}