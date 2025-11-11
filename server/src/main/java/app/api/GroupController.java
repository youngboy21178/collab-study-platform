package app.api;

import app.db.entities.Group;
import app.dto.groups.CreateGroupRequest;
import app.dto.groups.UpdateGroupAvatarRequest;
import app.services.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        try {
            Group group = groupService.createGroup(request);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public List<Group> getGroups() {
        return groupService.getAllGroups();
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> addMember(@PathVariable Long groupId,
                                       @PathVariable Long userId) {
        try {
            groupService.addMember(groupId, userId);
            return ResponseEntity.ok("member added");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long groupId) {
        List<Long> userIds = groupService.getGroupMemberUserIds(groupId);
        return ResponseEntity.ok(userIds);
    }

    @PutMapping("/{groupId}/avatar")
    public ResponseEntity<?> updateAvatar(@PathVariable Long groupId,
                                          @RequestBody UpdateGroupAvatarRequest request) {
        try {
            Group updated = groupService.updateGroupAvatar(groupId, request.getAvatarUrl());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{groupId}/avatar/file")
    public ResponseEntity<?> uploadGroupAvatar(@PathVariable Long groupId,
                                               @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("file is empty");
        }

        try {
            String uploadsDir = "uploads/groups";
            Path uploadPath = Paths.get(uploadsDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = groupId + ".png";
            Path target = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/uploads/groups/" + filename;

            Group group = groupService.getGroupById(groupId);
            group.setAvatarUrl(avatarUrl);
            Group saved = groupService.save(group);

            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("failed to save file");
        }
    }

}
