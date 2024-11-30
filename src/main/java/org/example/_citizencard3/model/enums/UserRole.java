package org.example._citizencard3.model.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ROLE_USER("一般用戶"),
    ROLE_ADMIN("系統管理員");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    // 從字串轉換為枚舉
    public static UserRole fromString(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ROLE_USER; // 預設為一般用戶
        }
    }

    // 檢查是否為管理員
    public boolean isAdmin() {
        return this == ROLE_ADMIN;
    }

    // 檢查是否為一般用戶
    public boolean isUser() {
        return this == ROLE_USER;
    }

    // 獲取角色名稱（不含 ROLE_ 前綴）
    public String getRoleName() {
        return this.name().substring(5);
    }
}