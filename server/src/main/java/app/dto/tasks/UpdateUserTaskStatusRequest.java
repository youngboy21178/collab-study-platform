package app.dto.tasks;

public class UpdateUserTaskStatusRequest {

    private Long userId;
    private String status; // OPEN / IN_PROGRESS / DONE

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
