package ru.accouting.student.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.accouting.student.model.StudyGroup;
import ru.accouting.student.repository.StudyGroupRepository;

@RestController
@RequiredArgsConstructor
public class StudyGroupRestController {

    private final StudyGroupRepository studyGroupRepository;

    @GetMapping("/groups/{id}/info")
    public GroupInfoDto getGroupInfo(@PathVariable Long id) {
        StudyGroup g = studyGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Группа не найдена: " + id));
        return new GroupInfoDto(
                g.getCourse(),
                g.getSpecialty().getCodeSpecialty(),
                g.getSpecialty().getTitleSpecialty()
        );
    }

    public record GroupInfoDto(Integer course, String specialtyCode, String specialtyTitle) {}
}
