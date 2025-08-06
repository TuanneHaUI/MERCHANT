package com.teamphacode.MerchantManagement.constant;

public enum ActionType {
    CREATE("Tạo mới"),
    UPDATE("Cập nhật"),
    DELETE("Xóa"),
    ACTIVATE("Kích hoạt"),
    DEACTIVATE("Vô hiệu hóa");

    private final String description;

    ActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}