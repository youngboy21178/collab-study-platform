package app.dto.groups;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateGroupRequest {

    @NotNull
    private Long ownerUserId;

    @NotBlank
    private String name;

    private String description;

    private String avatarUrl;

    // --- 1. Порожній конструктор (Обов'язковий для JSON) ---
    public CreateGroupRequest() {
    }

    // --- 2. Конструктор з даними ---
    public CreateGroupRequest(Long ownerUserId, String name, String description, String avatarUrl) {
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.description = description;
        this.avatarUrl = avatarUrl;
    }

    // --- 3. Геттери та Сеттери ---

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}