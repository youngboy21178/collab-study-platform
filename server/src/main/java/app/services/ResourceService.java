package app.services;

import app.db.entities.Group;
import app.db.entities.Resource;
import app.db.entities.User;
import app.db.repositories.GroupRepository;
import app.db.repositories.MembershipRepository;
import app.db.repositories.ResourceRepository;
import app.db.repositories.UserRepository;
import app.dto.resources.ResourceDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    // Папка 'uploads' створиться в корені проекту
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    // Ручний конструктор (замість Lombok @RequiredArgsConstructor)
    public ResourceService(ResourceRepository resourceRepository,
                           GroupRepository groupRepository,
                           UserRepository userRepository,
                           MembershipRepository membershipRepository) {
        this.resourceRepository = resourceRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;

        // Створення папки при ініціалізації сервісу
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Не вдалося створити директорію для файлів.", ex);
        }
    }

    @Transactional
    public ResourceDto uploadResource(Long groupId, Long uploaderId, MultipartFile file) {
        // 1. Пошук сутностей
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        // 2. ПЕРЕВІРКА ПРАВ: Чи є юзер учасником групи?
        boolean isMember = membershipRepository.findByGroupIdAndUserId(groupId, uploaderId).isPresent();
        if (!isMember) {
            throw new SecurityException("Ви не є учасником цієї групи, доступ заборонено.");
        }

        // 3. Збереження файлу на диск
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) originalFilename = "file";
        
        // Генеруємо унікальне ім'я (UUID)
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Помилка запису файлу на диск", ex);
        }

        // 4. Збереження в БД
        Resource resource = new Resource();
        resource.setFilename(originalFilename);
        resource.setStoredFilename(storedFilename);
        resource.setFileType(file.getContentType());
        resource.setSize(file.getSize());
        resource.setUploadedAt(LocalDateTime.now());
        resource.setGroup(group);
        resource.setUploader(uploader);

        Resource saved = resourceRepository.save(resource);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> getAllAccessibleResources(Long userId) {
        return resourceRepository.findAllAccessibleResources(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> getGroupResources(Long groupId, Long userId) {
        if (membershipRepository.findByGroupIdAndUserId(groupId, userId).isEmpty()) {
            throw new SecurityException("Немає доступу до ресурсів цієї групи.");
        }

        return resourceRepository.findByGroup_GroupIdOrderByUploadedAtDesc(groupId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public org.springframework.core.io.Resource loadFileAsResource(Long resourceId, Long userId) {
        Resource resourceEntity = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new EntityNotFoundException("Ресурс не знайдено"));

        Long groupId = resourceEntity.getGroup().getGroupId();
        if (membershipRepository.findByGroupIdAndUserId(groupId, userId).isEmpty()) {
            throw new SecurityException("Немає прав на скачування цього файлу.");
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(resourceEntity.getStoredFilename()).normalize();
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new EntityNotFoundException("Файл фізично відсутній на сервері.");
            }
        } catch (MalformedURLException ex) {
            throw new EntityNotFoundException("Помилка шляху файлу.", ex);
        }
    }
    
    public Resource getResourceEntity(Long id) {
        return resourceRepository.findById(id).orElseThrow();
    }

    private ResourceDto mapToDto(Resource r) {
        // Використовуємо наш ручний Builder
        return ResourceDto.builder()
                .id(r.getResourceId())
                .filename(r.getFilename())
                .fileType(r.getFileType())
                .size(r.getSize())
                .uploadedAt(r.getUploadedAt())
                .downloadUrl("/api/resources/" + r.getResourceId() + "/download")
                .uploaderName(r.getUploader().getName())
                .groupId(r.getGroup().getGroupId())
                .groupName(r.getGroup().getName())
                .build();
    }
}