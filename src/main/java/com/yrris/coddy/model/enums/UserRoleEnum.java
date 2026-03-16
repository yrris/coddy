package com.yrris.coddy.model.enums;

public enum UserRoleEnum {
    USER,
    ADMIN;

    public static boolean isAdmin(UserRoleEnum role) {
        return role == ADMIN;
    }
}
