package app.api;

import app.dto.resources.ResourceDto;
import app.services.ResourceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ResourceController {

    private final ResourceService resourceService;

    // Ручний конструктор замість @RequiredArgsConstructor
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    // 1. Завантажити файл у групу
    @PostMapping(value = "/groups/{groupId}/resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResourceDto> uploadResource(
            @PathVariable Long groupId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            @RequestParam(required = false) Long userIdParam) {

        Long userId = resolveUserId(authentication, userIdParam);
        ResourceDto created = resourceService.uploadResource(groupId, userId, file);
        return ResponseEntity.ok(created);
    }

    // 2. Всі ресурси користувача
    @GetMapping("/resources/my")
    public ResponseEntity<List<ResourceDto>> getMyResources(
            Authentication authentication,
            @RequestParam(required = false) Long userIdParam) {
        
        Long userId = resolveUserId(authentication, userIdParam);
        List<ResourceDto> resources = resourceService.getAllAccessibleResources(userId);
        return ResponseEntity.ok(resources);
    }

    // 3. Ресурси конкретної групи
    @GetMapping("/groups/{groupId}/resources")
    public ResponseEntity<List<ResourceDto>> getGroupResources(
            @PathVariable Long groupId,
            Authentication authentication,
            @RequestParam(required = false) Long userIdParam) {
        
        Long userId = resolveUserId(authentication, userIdParam);
        List<ResourceDto> list = resourceService.getGroupResources(groupId, userId);
        return ResponseEntity.ok(list);
    }

    // 4. Скачати файл
    // Використовуємо повний шлях org.springframework.core.io.Resource, щоб уникнути конфлікту імен
    @GetMapping("/resources/{resourceId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(
            @PathVariable Long resourceId,
            Authentication authentication,
            @RequestParam(required = false) Long userIdParam) {

        Long userId = resolveUserId(authentication, userIdParam);
        
        // Отримуємо файл (фізичний ресурс)
        org.springframework.core.io.Resource fileResource = resourceService.loadFileAsResource(resourceId, userId);
        
        // Отримуємо метадані (сутність БД) - використовуємо повний шлях
        app.db.entities.Resource dbResource = resourceService.getResourceEntity(resourceId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dbResource.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbResource.getFilename() + "\"")
                .body(fileResource);
    }

    private Long resolveUserId(Authentication auth, Long paramId) {
        if (paramId != null) return paramId;

        if (auth != null && auth.getPrincipal() instanceof app.db.entities.User) {
            return ((app.db.entities.User) auth.getPrincipal()).getUserId();
        }
        
        throw new SecurityException("User ID not found via Auth or Param");
    }
}