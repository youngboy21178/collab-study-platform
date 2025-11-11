package app.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserProfileRequest {

    @NotBlank
    private String name;

    private String avatarUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
