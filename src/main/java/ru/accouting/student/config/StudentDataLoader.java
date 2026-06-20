package ru.accouting.student.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.accouting.student.model.*;
import ru.accouting.student.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(2)
@RequiredArgsConstructor
public class StudentDataLoader implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final StudentCredentialsRepository studentCredentialsRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final MilitaryCommissariatRepository militaryCommissariatRepository;
    private final MilitaryAccountingSpecialtyEntityRepository militaryAccountingSpecialtyEntityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (studentRepository.count() > 0) {
            return;
        }

        List<StudyGroup> groups = studyGroupRepository.findAll(Sort.by("nameGroup"));
        List<MilitaryCommissariatEntity> commissariats = militaryCommissariatRepository.findAll(Sort.by("name"));
        List<MilitaryAccountingSpecialtyEntity> vusList = militaryAccountingSpecialtyEntityRepository.findAll(Sort.by("code"));

        if (groups.isEmpty() || commissariats.isEmpty() || vusList.isEmpty()) {
            return;
        }

        List<String> lastNames = List.of(
                "Иванов", "Петров", "Сидоров", "Кузнецов", "Смирнов",
                "Васильев", "Попов", "Соколов", "Михайлов", "Новиков",
                "Фёдоров", "Волков", "Алексеев", "Лебедев", "Семёнов",
                "Егоров", "Павлов", "Козлов", "Степанов", "Николаев",
                "Орлов", "Андреев", "Макаров", "Никитин", "Захаров",
                "Зайцев", "Соловьёв", "Борисов", "Яковлев", "Григорьев",
                "Романов", "Воробьёв", "Сергеев", "Кузьмин", "Фролов",
                "Александров", "Дмитриев", "Королёв", "Гусев", "Киселёв",
                "Ильин", "Максимов", "Поляков", "Сорокин", "Виноградов",
                "Ковалёв", "Белов", "Медведев", "Антонов", "Тарасов",
                "Жуков", "Баранов", "Филиппов", "Комаров", "Давыдов",
                "Беляев", "Тимофеев", "Богданов", "Басов", "Журавлёв",
                "Герасимов", "Матвеев", "Владимиров", "Суханов", "Анисимов",
                "Соколовский", "Елисеев", "Калинин", "Никитенко", "Сафонов",
                "Рыбаков", "Панфилов", "Кудрявцев", "Шестаков", "Шаров",
                "Назаров", "Гончаров", "Чернов", "Логинов", "Быков",
                "Овчинников", "Гаврилов", "Тихонов", "Мартынов", "Ершов",
                "Рябов", "Кудинов", "Осипов", "Карпов", "Корнеев",
                "Плотников", "Капустин", "Быстров", "Фомичёв", "Селезнёв",
                "Ершов", "Нечаев", "Маслов", "Черкасов", "Фомин"
        );

        List<String> firstNames = List.of(
                "Александр", "Дмитрий", "Иван", "Сергей", "Андрей",
                "Максим", "Артем", "Михаил", "Никита", "Егор",
                "Алексей", "Артемий", "Илья", "Данил", "Кирилл",
                "Павел", "Роман", "Владимир", "Владислав", "Георгий",
                "Глеб", "Денис", "Евгений", "Константин", "Лев",
                "Леонид", "Матвей", "Николай", "Олег", "Петр",
                "Руслан", "Станислав", "Степан", "Тимофей", "Федор",
                "Ярослав", "Богдан", "Вадим", "Виктор", "Виталий",
                "Вячеслав", "Григорий", "Давид", "Захар", "Марк",
                "Марат", "Мирон", "Назар", "Платон", "Прохор",
                "Савелий", "Святослав", "Тарас", "Тихон", "Эдуард",
                "Юрий", "Амир", "Арсен", "Артур", "Борис",
                "Валентин", "Василий", "Герман", "Игорь", "Иннокентий",
                "Клим", "Макар", "Марат", "Мстислав", "Нестор",
                "Оскар", "Павел", "Платон", "Родион", "Семен",
                "Тимур", "Устин", "Феликс", "Эмиль", "Юлиан",
                "Ян", "Аким", "Анатолий", "Антон", "Аркадий",
                "Альберт", "Бенедикт", "Вениамин", "Гавриил", "Елисей",
                "Игнат", "Лука", "Сава", "Серафим", "Филипп",
                "Эрик", "Яков", "Аристарх", "Валерий", "Демьян"
        );

        Map<Integer, Integer> yearDistribution = Map.of(
                2024, 200,
                2025, 350,
                2026, 242
        );

        List<Student> toSave = new ArrayList<>();
        List<PasswordRow> passwords = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);

        for (Map.Entry<Integer, Integer> entry : yearDistribution.entrySet()) {
            int year = entry.getKey();
            int count = entry.getValue();

            for (int i = 0; i < count; i++) {
                int index = counter.getAndIncrement();

                Random random = ThreadLocalRandom.current();
                String lastName = lastNames.get(random.nextInt(lastNames.size()));
                String firstName = firstNames.get(random.nextInt(firstNames.size()));
                LocalDate birthday = LocalDate.of(2001 + (index % 5), 1 + (index % 12), 1 + (index % 28));
                String rawPassword = generatePassword(10);
                String login = "login" + String.format("%06d", index);

                Student s = createStudent(
                        lastName, firstName, "Отчество", birthday,
                        groups.get(index % groups.size()),
                        commissariats.get(index % commissariats.size()),
                        vusList.get(index % vusList.size()),
                        "ЦА-" + String.format("%06d", index),
                        "+79000000000", "", "4000", String.format("%06d", index), "Москва", birthday.toString(),
                        login
                );

                s.setApplicationYear(year);
                toSave.add(s);
                passwords.add(new PasswordRow(login, rawPassword));
            }
        }

        List<Student> savedStudents = studentRepository.saveAll(toSave);

        for (int i = 0; i < savedStudents.size(); i++) {
            Student s = savedStudents.get(i);
            PasswordRow row = passwords.get(i);

            StudentCredentials credentials = new StudentCredentials();
            credentials.setStudent(s);
            credentials.setLogin(row.login());
            credentials.setRawPassword(row.rawPassword());
            credentials.setCreatedAt(LocalDateTime.now());
            credentials.setExported(false);

            studentCredentialsRepository.save(credentials);
        }
    }

    private Student createStudent(String lastName, String firstName, String patronymic, LocalDate birthday,
                                  StudyGroup group, MilitaryCommissariatEntity mc, MilitaryAccountingSpecialtyEntity vus,
                                  String idCard, String phone, String note, String series, String number, String place, String datePas,
                                  String login) {

        Student s = new Student();
        s.setLastName(lastName);
        s.setFirstName(firstName);
        s.setPatronymic(patronymic);
        s.setBirthday(birthday);
        s.setGroupStudent(group);
        s.setCourse(group != null ? group.getCourse() : 1);
        s.setMilitaryCommissariat(mc);
        s.setMilitaryAccountingSpecialty(vus);
        s.setStudentIdCard(idCard);
        s.setPhoneNumber(phone);
        s.setNoteStudent(note);
        s.setStatus(StudentStatus.APPLIED);

        Passport passport = new Passport();
        passport.setSeriesPassport(series);
        passport.setNumberPassport(number);
        passport.setPlacePassport(place);
        passport.setDatePassport(datePas);
        passport.setStudent(s);
        s.setPassport(passport);

        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(generatePassword(10)));
        user.setStudent(s);

        UserRole role = new UserRole();
        role.setUserAuthority(UserAuthority.USER);
        role.setUser(user);

        user.setUserRoles(List.of(role));
        s.setUser(user);

        return s;
    }

    private String generatePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private record PasswordRow(String login, String rawPassword) {}
}