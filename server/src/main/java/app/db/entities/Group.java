package app.db.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    // --- ДОДАНО: Поле, через яке падала помилка ---
    @Column(name = "created_by")
    private Long createdBy;
    // ---------------------------------------------

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    // --- Геттери та Сеттери ---

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    // --- ДОДАНО: Геттер і Сеттер для createdBy ---
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    // ---------------------------------------------

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}