package app.services;

import app.db.entities.Group;
import app.db.entities.Membership;
import app.db.entities.User;
import app.db.repositories.GroupRepository;
import app.db.repositories.MembershipRepository;
import app.db.repositories.UserRepository;
import app.dto.groups.CreateGroupRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public GroupService(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            UserRepository userRepository
    ) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Group createGroup(CreateGroupRequest request) {
        User owner = userRepository.findById(request.getOwnerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Owner user not found"));

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setOwnerUserId(owner.getUserId());

        // --- ВИПРАВЛЕННЯ ПОМИЛКИ 500 (SQL CONSTRAINT) ---
        // Ми встановлюємо поле createdBy, яке вимагає база даних
        group.setCreatedBy(owner.getUserId());
        // -----------------------------------------------

        group.setAvatarUrl(request.getAvatarUrl());
        group.setCreatedAt(Instant.now().toString());

        Group saved = groupRepository.save(group);

        Membership ownerMembership = new Membership();
        ownerMembership.setUserId(owner.getUserId());
        ownerMembership.setGroupId(saved.getGroupId());
        ownerMembership.setRole("OWNER");
        ownerMembership.setJoinedAt(Instant.now().toString());
        membershipRepository.save(ownerMembership);

        return saved;
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }

    public List<Long> getGroupMemberUserIds(Long groupId) {
        List<Membership> memberships = membershipRepository.findByGroupId(groupId);
        return memberships.stream()
                .map(Membership::getUserId)
                .collect(Collectors.toList());
    }

    @Transactional
    public Group uploadAvatarFile(Long groupId, MultipartFile file) {
        Group group = getGroupById(groupId);

        try {
            // 1. Папка для груп
            Path uploadDir = Paths.get("uploads", "groups");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 2. Ім'я файлу = ID групи
            String filename = groupId + ".png";
            Path filePath = uploadDir.resolve(filename);

            // 3. Зберігаємо
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. Оновлюємо посилання в БД
            String fileUrl = "/uploads/groups/" + filename;
            group.setAvatarUrl(fileUrl);

            return groupRepository.save(group);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save group avatar", e);
        }
    }

    @Transactional
    public void updateGroupAvatar(Long groupId, String avatarUrl) {
        Group group = getGroupById(groupId);
        group.setAvatarUrl(avatarUrl);
        groupRepository.save(group);
    }

    @Transactional
    public void addMember(Long groupId, Long userId) {
        Membership membership = new Membership();
        membership.setGroupId(groupId);
        membership.setUserId(userId);
        membership.setRole("MEMBER");
        membership.setJoinedAt(Instant.now().toString());
        membershipRepository.save(membership);
    }

    @Transactional
    public void removeMember(Long groupId, Long userId) {
        Group group = getGroupById(groupId);
        if (group.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException("Cannot kick the owner of the group!");
        }

        Membership membership = membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));

        membershipRepository.delete(membership);
    }

    public Group save(Group group) {
        return groupRepository.save(group);
    }

    public List<Group> getUserGroups(Long userId) {
        // 1. Групи, де я власник
        List<Group> ownedGroups = groupRepository.findByOwnerUserId(userId);

        // 2. Групи, де я учасник (через таблицю Membership)
        List<Membership> memberships = membershipRepository.findByUserId(userId);
        List<Long> joinedGroupIds = memberships.stream()
                .map(Membership::getGroupId)
                .collect(Collectors.toList());
        List<Group> joinedGroups = groupRepository.findAllById(joinedGroupIds);

        // 3. Об'єднуємо (Set автоматично прибирає дублікати)
        java.util.Set<Group> allGroups = new java.util.HashSet<>();
        allGroups.addAll(ownedGroups);
        allGroups.addAll(joinedGroups);

        return new java.util.ArrayList<>(allGroups);
    }
}