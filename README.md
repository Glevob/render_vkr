# studentAccounting

Веб-приложение для автоматизации ведения воинского учета с обеспечением криптографической защиты персональных данных.

# Описание предметной области:
- Система предназначена для комплексного администрирования данных о студентах, учебных группах и взводах.
- Хранение профилей студентов с привязкой к учебным группам (StudyGroup).
- Ведение воинского учета: распределение студентов по взводам (Platoon), ведение военно-учетных специальностей (ВУС) и логирование процессов перевода между взводами.
- Автоматический расчет, анализ и мониторинг результатов выполнения нормативов по физической подготовке студентов (PhysicalCalculationService).
- Безопасное хранение конфиденциальной информации (паспортные данные) с использованием сквозного шифрования по алгоритму AES-GCM на уровне базы данных при помощи JPA-конвертеров.
- Аутентификация и авторизация пользователей, разграничение прав доступа к административным операциям, изменению данных и просмотру отчетов.

# Стек технологий:
- Java 21
- Spring Boot 3 / Spring Web / Spring Data JPA / Spring Security
- Hibernate
- PostgreSQL
- Liquibase
- Thymeleaf для веб‑страниц интерфейса
- Gradle для сборки

# Как запустить проект:
- Настройка БД PostgreSQL.
- Сборка и запуск приложения с помощью Gradle.
- Запуск веб-интерфейса - http://localhost:8080
- 
## Структура
```text
.
├── gradlew
├── gradlew.bat
├── build.gradle
├── settings.gradle
└── src/
    └── main/
        ├── java/
        │   └── ru/
        │       └── accouting/
        │           └── student/
        │               ├── StudentApplication.java   # Точка входа Spring Boot
        │               ├── config/                   # Конфигурация шифрования (CryptoConfig)
        │               ├── controllers/              # Web-контроллеры и REST API (StudentController, AuthRestController)
        │               ├── models/                   # Доменная модель (Student, Passport, Platoon, StudyGroup, MAS)
        │               ├── repositories/             # Слой доступа к данным (StudentRepository, UserRepository)
        │               └── services/                 # Бизнес-логика (PlatoonTransferService, PhysicalCalculationService)
        └── resources/
            ├── application.properties                # Конфигурация окружения и СУБД
            ├── db/
            │   └── changelog/                        # Миграции Liquibase (YAML-чейнджлоги)
            └── templates/                            # Интерфейс на базе Thymeleaf
```

# Требования
- Java Development Kit (JDK) 17+
- СУБД PostgreSQL 15+

# Тестовые логины (bcrypt в БД):

| Логин | Пароль | Роль |
|-------|--------|------|
| `asd` | `asdasd` | admin |
| `asdasd` | `asdasd` | technologist |
