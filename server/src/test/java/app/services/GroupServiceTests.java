package app.services;

import app.db.entities.Group;
import app.db.repositories.GroupRepository;
import app.db.repositories.MembershipRepository;
import app.db.repositories.UserRepository;
import app.dto.groups.CreateGroupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GroupServiceTests {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @BeforeEach
    void cleanDb() {
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createGroup_createsGroupAndOwnerMembership() {
        // create owner user
        var user = new app.db.entities.User();
        user.setName("Owner");
        user.setEmail("owner@test.com");
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Test group");
        request.setDescription("desc");
        request.setOwnerUserId(user.getUserId());
        request.setAvatarUrl(null);

        Group group = groupService.createGroup(request);

        assertNotNull(group.getGroupId());
        assertEquals("Test group", group.getName());

        var members = membershipRepository.findByGroupId(group.getGroupId());
        assertEquals(1, members.size());
        assertEquals(user.getUserId(), members.get(0).getUserId());
        assertEquals("OWNER", members.get(0).getRole());
    }
}
