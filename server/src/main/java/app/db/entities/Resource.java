package app.db.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @Column(nullable = false)
    private String filename; // Оригінальна назва файлу (звіт.pdf)

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename; // Унікальна назва на диску (uuid_звіт.pdf)

    @Column(name = "file_type")
    private String fileType; // MIME type (application/pdf)

    @Column(nullable = false)
    private Long size; // Розмір у байтах

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    // Зв'язок з групою
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // Зв'язок з автором (uploader)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    // Геттери та Сеттери
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String storedFilename) { this.storedFilename = storedFilename; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public User getUploader() { return uploader; }
    public void setUploader(User uploader) { this.uploader = uploader; }
}