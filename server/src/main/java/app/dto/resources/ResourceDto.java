package app.dto.resources;

import java.time.LocalDateTime;

public class ResourceDto {
    private Long id;
    private String filename;
    private String fileType;
    private String downloadUrl;
    private Long size;
    private String uploaderName;
    private Long groupId;
    private String groupName;
    private LocalDateTime uploadedAt;

    public ResourceDto() {
    }

    public ResourceDto(Long id, String filename, String fileType, String downloadUrl, Long size, String uploaderName, Long groupId, String groupName, LocalDateTime uploadedAt) {
        this.id = id;
        this.filename = filename;
        this.fileType = fileType;
        this.downloadUrl = downloadUrl;
        this.size = size;
        this.uploaderName = uploaderName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.uploadedAt = uploadedAt;
    }

    // Статичний метод для початку побудови
    public static Builder builder() {
        return new Builder();
    }

    // Внутрішній клас Builder
    public static class Builder {
        private Long id;
        private String filename;
        private String fileType;
        private String downloadUrl;
        private Long size;
        private String uploaderName;
        private Long groupId;
        private String groupName;
        private LocalDateTime uploadedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder filename(String filename) { this.filename = filename; return this; }
        public Builder fileType(String fileType) { this.fileType = fileType; return this; }
        public Builder downloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; return this; }
        public Builder size(Long size) { this.size = size; return this; }
        public Builder uploaderName(String uploaderName) { this.uploaderName = uploaderName; return this; }
        public Builder groupId(Long groupId) { this.groupId = groupId; return this; }
        public Builder groupName(String groupName) { this.groupName = groupName; return this; }
        public Builder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }

        public ResourceDto build() {
            return new ResourceDto(id, filename, fileType, downloadUrl, size, uploaderName, groupId, groupName, uploadedAt);
        }
    }

    // Геттери та Сеттери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}