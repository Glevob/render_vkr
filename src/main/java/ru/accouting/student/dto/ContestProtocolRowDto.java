package ru.accouting.student.dto;

import lombok.Getter;
import lombok.Setter;
import ru.accouting.student.model.MilitaryAccountingSpecialtyEntity;
import ru.accouting.student.model.Student;
import ru.accouting.student.model.StudentStatus;

@Getter
@Setter
public class ContestProtocolRowDto {

    private Long studentId;

    private String fullName;      // ФИО
    private String birthDate;     // Дата рождения
    private String specialtyCode; // Код специальности
    private String groupName;     // Наимнование учебной группы
    private Integer course;       // Курс обучения

    // результаты предварительного отбора
    private String fitnessCategory;    // Категория годности (А, Б, В, Г, Д)
    private String psychoCategory;     // Категория ППО (I, II, III, IV)

    private String ageGroup;           // возрастная группа

    // физподготовка: три упражнения
    private Integer strengthExerciseNumber;  // Номер упражнения на Силу
    private String strengthResult;           // Результат студена
    private Integer strengthPoints;          // Балл за Упражнение

    private Integer speedExerciseNumber;     // Номер упражнения на Быстроту
    private String speedResult;              // Результа тстудента
    private Integer speedPoints;             // Балл за Упражнение

    private Integer enduranceExerciseNumber; // Номер Упражнения на Выносливость
    private String enduranceResult;          // Результат Упражнения на Выносливость
    private Integer endurancePoints;         // Балл за Упражнение

    private Integer totalPoints;             // Общий балл

    private String physicalRequirementsMatch; // «соответствует / не соответствует»
    private Integer physical100;              // оценка уровня физ. подготовки по 100‑балльной шкале

    // льготы
    private boolean hasQuotaRight;       // право допуска =>10% (квота)
    private boolean hasPriorityRight;    // преимущественное право допуска

    private Integer academic100;         // оценка текущей успеваемости (100‑балльная)
    private Integer finalScore;          // итоговый результат (фп + учёба)
    private String commissionDecision;   // решение комиссии

    private StudentStatus status;        // Статус в системе
    private MilitaryAccountingSpecialtyEntity militaryAccountingSpecialty; // ВУС

    private Long platoonId;     // ID взвода
    private String platoonName; // Наоменование взвода

    private String programCode;             // Код программы подготовки (для группировки)
    private String universitySpecialtyCode; // Код специальности ВУЗа (для таблицы)

    private String noteStudent;

    public String getFitnessCategoryLabel() {
        if (fitnessCategory == null || fitnessCategory.isEmpty()) return "—";
        try {
            return Student.FitnessCategory.valueOf(fitnessCategory).getLabel();
        } catch (IllegalArgumentException e) {
            return fitnessCategory; // Если данные в БД не соответствуют Enum
        }
    }

    public String getCommissionDecisionFormatted() {
        String decision = (this.commissionDecision != null) ? this.commissionDecision : "—";

        // Если решение не связано с категорией, возвращаем как есть
        if (!decision.toLowerCase().contains("годн")) {
            return decision;
        }

        String rusLabel = getFitnessCategoryLabel();

        String letter = rusLabel.split(" — ")[0];

        // Заменяем английские буквы в строке решения на русские буквы
        // Используем replace, чтобы поменять C на В, A на А, B на Б и т.д.
        return decision
                .replace("A", "А")
                .replace("B", "Б")
                .replace("C", "В")
                .replace("D", "Г")
                .replace("E", "Д")
                // Если в строке осталась "Категория:", убираем её, чтобы не дублировалось
                .replace("(Категория: " + letter + ")", "");
    }
}
