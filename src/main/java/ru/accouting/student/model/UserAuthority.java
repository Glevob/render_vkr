package ru.accouting.student.model;

import org.springframework.security.core.GrantedAuthority;

public enum UserAuthority implements GrantedAuthority {

    USER, // Студент
    TECHNOLOGIST, // Сотрудник с правами управления данными
    FULL; // Админ, который может менять права доступа

    @Override
    public String getAuthority() {
        return this.name();
    }
}

