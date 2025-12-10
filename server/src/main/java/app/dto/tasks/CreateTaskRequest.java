package app.dto.tasks;

public class CreateTaskRequest {

    private Long groupId;
    private Long creatorUserId;
    private String title;
    private String description;
    private String dueDate; // ISO string, e.g. "2025-12-31"

    
    public CreateTaskRequest() {
    }

    public CreateTaskRequest(Long groupId, Long creatorUserId, String title, String description, String dueDate) {
        this.groupId = groupId;
        this.creatorUserId = creatorUserId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(Long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}