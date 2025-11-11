package app.services;

import app.db.entities.Group;
import app.db.entities.Membership;
import app.db.entities.User;
import app.db.repositories.GroupRepository;
import app.db.repositories.MembershipRepository;
import app.db.repositories.UserRepository;
import app.dto.groups.CreateGroupRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository,
                        MembershipRepository membershipRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    public Group createGroup(CreateGroupRequest request) {
        User owner = userRepository.findById(request.getOwnerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Owner user not found"));

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(owner.getUserId());
        group.setCreatedAt(LocalDateTime.now().toString());
        group.setAvatarUrl(request.getAvatarUrl());

        Group saved = groupRepository.save(group);

        Membership m = new Membership();
        m.setGroupId(saved.getGroupId());
        m.setUserId(owner.getUserId());
        m.setRole("OWNER");
        m.setJoinedAt(LocalDateTime.now().toString());

        membershipRepository.save(m);

        return saved;
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public void addMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (membershipRepository.existsByGroupIdAndUserId(group.getGroupId(), user.getUserId())) {
            return;
        }

        Membership m = new Membership();
        m.setGroupId(group.getGroupId());
        m.setUserId(user.getUserId());
        m.setRole("MEMBER");
        m.setJoinedAt(LocalDateTime.now().toString());

        membershipRepository.save(m);
    }

    public List<Long> getGroupMemberUserIds(Long groupId) {
        return membershipRepository.findByGroupId(groupId)
                .stream()
                .map(Membership::getUserId)
                .collect(Collectors.toList());
    }

    public Group updateGroupAvatar(Long groupId, String avatarUrl) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        group.setAvatarUrl(avatarUrl);
        return groupRepository.save(group);
    }
}
