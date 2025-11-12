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

    public Group save(Group group) {
        return groupRepository.save(group);
    }
}
