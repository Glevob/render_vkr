package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@ToString
@Entity(name="student")
@Table(name="student")
@Getter
@Setter
public class Student {

    @Id
    @Column(name="id_student")
    @GeneratedValue(generator = "id_student_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "id_student_seq", sequenceName = "id_student_seq", initialValue = 1, allocationSize = 1)
    private Long idStudent;

    // Имя студента
    @Column(name="firstName")
    private String firstName;

    // Фамилия студента
    @Column(name="lastName")
    private String lastName;

    // Отчество студента
    @Column(name="patronymic")
    private String patronymic;

    // дата рождения студента
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name="birthday")
    private LocalDate birthday;

    // Курс обучения в ВУЗе
    @Column(name="course")
    private Integer course;

    // Группа студента
    @ManyToOne
    @JoinColumn(name = "study_group_id")
    private StudyGroup groupStudent;

    // Военный комиссариат
    @ManyToOne
    @JoinColumn(name = "military_commissariat_id")
    private MilitaryCommissariatEntity militaryCommissariat;

    // Выбранная студентом ВУС
    @ManyToOne
    @JoinColumn(name = "military_accounting_specialty_id")
    private MilitaryAccountingSpecialtyEntity militaryAccountingSpecialty;

    // Номер студенческого билета
    @Column(name="studentIdCard")
    private String studentIdCard;

    // Номер мобильного телефона
    @Column(name="phoneNumber")
    private String phoneNumber;

    // Льгота
    @Column(name = "noteStudent", length = 255)
    private String noteStudent;

    // Паспорт студента
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "passport", unique = true)
    private Passport passport;

    // Статус в системе ВУЦ
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StudentStatus status;

    // Взвод
    @ManyToOne
    @JoinColumn(name = "platoon_id")
    private Platoon platoon;

    // Результаты физической подготовки
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private PhysicalTraining physicalTraining;

    // Категория годности
    @Enumerated(EnumType.STRING)
    private FitnessCategory fitnessCategory;

    // Категория ППО
    @Enumerated(EnumType.STRING)
    private PsychoCategory psychoCategory;

    // Средний балл успеваемости в ВУЗе (3.00-5.00)
    @Column(precision = 3, scale = 2)
    private BigDecimal grade5;

    // Средний балл успеваемости в 100-балльной шкале
    @Column(precision = 5, scale = 2)
    private BigDecimal grade100;

    // Год подачи заявления
    @Column(name = "application_year")
    private Integer applicationYear;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private StudentCredentials studentCredentials;

    // ---------- ВОЗРАСТ ----------

    // Получение возраста студента
    @Transient
    public Integer getAge() {
        if (birthday == null) {
            return null;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    // Проверка: младше ли 25 лет студент
    @Transient
    public boolean isUnder25() {
        Integer age = getAge();
        return age != null && age < 25;
    }

    // Проверка: старше ли 25 лет студент
    @Transient
    public boolean isOverOrEqual25() {
        Integer age = getAge();
        return age != null && age >= 25;
    }

    //Перевод категори годности на русский
    public enum FitnessCategory {
        A("А"),
        B("Б"),
        C("В"),
        D("Г"),
        E("Д");

        private final String label;

        FitnessCategory(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    //Варианты категории ППО
    public enum PsychoCategory {
        I, II, III, IV
    }

    // ---------- конструкторы ----------

    public Student() {
    }

    public Student(String firstName, String lastName, String patronymic, LocalDate birthday,
                   Integer course, StudyGroup groupStudent,
                   MilitaryCommissariatEntity militaryCommissariat,
                   MilitaryAccountingSpecialtyEntity militaryAccountingSpecialty,
                   String studentIdCard, String phoneNumber, String noteStudent) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
        this.birthday = birthday;
        this.course = course;
        this.groupStudent = groupStudent;
        this.militaryCommissariat = militaryCommissariat;
        this.militaryAccountingSpecialty = militaryAccountingSpecialty;
        this.studentIdCard = studentIdCard;
        this.phoneNumber = phoneNumber;
        this.noteStudent = noteStudent;
    }
}