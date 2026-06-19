package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Convert;
import ru.accouting.student.security.JpaCryptoConverter;
import ru.accouting.student.service.HashService;

@Entity
@Table(name = "passport")
@Getter
@Setter
public class Passport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Номер паспорта
    @Column(name = "number_passport", nullable = false)
    @Convert(converter = JpaCryptoConverter.class) // Шифрование данных в БД
    private String numberPassport;

    // Серия паспорта
    @Column(name = "series_passport", nullable = false)
    @Convert(converter = JpaCryptoConverter.class) // Шифрование данных в БД
    private String seriesPassport;

    // Место выдачи паспорта
    @Column(name = "place_passport")
    @Convert(converter = JpaCryptoConverter.class) // Шифрование данных в БД
    private String placePassport;

    // Дата выдачи паспорта
    @Column(name = "date_passport")
    @Convert(converter = JpaCryptoConverter.class) // Шифрование данных в БД
    private String datePassport;

    @OneToOne(mappedBy = "passport")
    private Student student;

    // Хэш паспортных данных для последующей сверки
    @Column(name = "passport_hash")
    private String passportHash;

    // Хэш серии и номера паспорта
    public void updateHash(String series, String number, HashService hashService) {
        this.passportHash = hashService.generatePassportHash(series, number);
    }
}