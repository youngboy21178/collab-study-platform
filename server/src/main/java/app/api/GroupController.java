package app.api;

import app.db.entities.Group;
import app.dto.groups.CreateGroupRequest;
import app.dto.groups.UpdateGroupAvatarRequest;
import app.services.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // Create group
    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequest request) {
        Group created = groupService.createGroup(request);
        return ResponseEntity.ok(created);
    }

    // List all groups
    @GetMapping
    public List<Group> getAllGroups(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return groupService.getUserGroups(userId);
        }
        // Якщо ID не передали — повертаємо всі (наприклад, для адміна)
        return groupService.getAllGroups();
    }



    // Get members (user IDs) of a group
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<Long>> getGroupMembers(@PathVariable Long groupId) {
        List<Long> memberIds = groupService.getGroupMemberUserIds(groupId);
        return ResponseEntity.ok(memberIds);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public void removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
    }

    // Update avatar by URL (simple version)
    @PutMapping("/{groupId}/avatar")
    public ResponseEntity<Group> updateGroupAvatar(
            @PathVariable Long groupId,
            @RequestBody UpdateGroupAvatarRequest request
    ) {
        // service method returns void -> just call it
        groupService.updateGroupAvatar(groupId, request.getAvatarUrl());

        // then load updated group and return it
        Group updated = groupService.getGroupById(groupId);
        return ResponseEntity.ok(updated);
    }
    @PutMapping(value = "/{groupId}/avatar/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Group uploadAvatar(@PathVariable Long groupId, @RequestParam("file") MultipartFile file) {
        return groupService.uploadAvatarFile(groupId, file);
    }

    // Add member to group
    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> addMember(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        groupService.addMember(groupId, userId);
        return ResponseEntity.ok().build();
    }

    // Update basic group data (name, description)
    @PutMapping("/{groupId}")
    public ResponseEntity<Group> updateGroup(
            @PathVariable Long groupId,
            @RequestBody CreateGroupRequest request
    ) {
        Group group = groupService.getGroupById(groupId);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        Group saved = groupService.save(group);
        return ResponseEntity.ok(saved);
    }
}
