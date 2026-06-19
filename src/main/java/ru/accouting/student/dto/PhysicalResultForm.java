package ru.accouting.student.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PhysicalResultForm {
    private List<PhysicalResultFormItem> items = new ArrayList<>();
}
