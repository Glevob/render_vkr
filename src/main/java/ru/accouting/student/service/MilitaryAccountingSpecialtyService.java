package ru.accouting.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.accouting.student.model.MilitaryAccountingSpecialtyEntity;
import ru.accouting.student.repository.MilitaryAccountingSpecialtyEntityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilitaryAccountingSpecialtyService {

    private final MilitaryAccountingSpecialtyEntityRepository repository;

    public List<MilitaryAccountingSpecialtyEntity> findAll() {
        return repository.findAll();
    }
}
