package app.services;

import app.db.entities.Group;
import app.db.entities.Task;
import app.db.entities.TaskProgress;
import app.db.entities.User;
import app.db.repositories.GroupRepository;
import app.db.repositories.MembershipRepository;
import app.db.repositories.TaskProgressRepository;
import app.db.repositories.TaskRepository;
import app.db.repositories.UserRepository;
import app.dto.groups.CreateGroupRequest;
import app.dto.tasks.CreateTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TaskServiceTests {

    @Autowired
    private TaskService taskService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskProgressRepository taskProgressRepository;

    @BeforeEach
    void cleanDb() {
        taskProgressRepository.deleteAll();
        taskRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void fullFlow_createTask_assignUser_and_updateUserStatus() {
        User user = new User();
        user.setName("Maksym");
        user.setEmail("maksym+task@test.com");
        user.setPasswordHash("dummy-hash");
        user = userRepository.save(user);

        CreateGroupRequest groupRequest = new CreateGroupRequest();
        groupRequest.setName("Algorithms Study");
        groupRequest.setDescription("Group for algorithms practice");
        groupRequest.setOwnerUserId(user.getUserId());
        groupRequest.setAvatarUrl(null);

        Group group = groupService.createGroup(groupRequest);
        assertNotNull(group.getGroupId());

        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setGroupId(group.getGroupId());
        taskRequest.setCreatorUserId(user.getUserId());
        taskRequest.setTitle("Solve 10 problems");
        taskRequest.setDescription("Solve 10 dynamic programming problems");
        taskRequest.setDueDate("2025-12-31");

        Task task = taskService.createTask(taskRequest);
        assertNotNull(task.getTaskId());
        assertEquals("Solve 10 problems", task.getTitle());
        assertEquals("OPEN", task.getStatus());
        assertEquals(group.getGroupId(), task.getGroupId());
        assertEquals(user.getUserId(), task.getCreatorUserId());

        TaskProgress progress = taskService.assignUserToTask(task.getTaskId(), user.getUserId());
        assertNotNull(progress.getId());
        assertEquals(task.getTaskId(), progress.getTaskId());
        assertEquals(user.getUserId(), progress.getUserId());
        assertEquals("OPEN", progress.getStatus());

        TaskProgress inProgress = taskService.updateUserTaskStatus(
                task.getTaskId(),
                user.getUserId(),
                "IN_PROGRESS"
        );
        assertEquals("IN_PROGRESS", inProgress.getStatus());
        assertNull(inProgress.getCompletedAt());

        TaskProgress done = taskService.updateUserTaskStatus(
                task.getTaskId(),
                user.getUserId(),
                "DONE"
        );
        assertEquals("DONE", done.getStatus());
        assertNotNull(done.getCompletedAt());

        List<Task> tasksForGroup = taskService.getTasksForGroup(group.getGroupId());
        assertEquals(1, tasksForGroup.size());
        Task fromList = tasksForGroup.get(0);
        assertEquals(task.getTaskId(), fromList.getTaskId());
    }

    @Test
    void multipleUsers_multipleTasks_progressFlowsCorrectly() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner.setPasswordHash("hash");
        owner = userRepository.save(owner);

        User userA = new User();
        userA.setName("User A");
        userA.setEmail("userA@test.com");
        userA.setPasswordHash("hash");
        userA = userRepository.save(userA);

        User userB = new User();
        userB.setName("User B");
        userB.setEmail("userB@test.com");
        userB.setPasswordHash("hash");
        userB = userRepository.save(userB);

        CreateGroupRequest groupRequest = new CreateGroupRequest();
        groupRequest.setName("Discrete Math");
        groupRequest.setDescription("Group for discrete math tasks");
        groupRequest.setOwnerUserId(owner.getUserId());
        groupRequest.setAvatarUrl(null);

        Group group = groupService.createGroup(groupRequest);
        assertNotNull(group.getGroupId());

        // add members using existing signature addMember(groupId, userId)
        groupService.addMember(group.getGroupId(), userA.getUserId());
        groupService.addMember(group.getGroupId(), userB.getUserId());

        CreateTaskRequest t1Req = new CreateTaskRequest();
        t1Req.setGroupId(group.getGroupId());
        t1Req.setCreatorUserId(owner.getUserId());
        t1Req.setTitle("Read lecture 1");
        t1Req.setDescription("Intro to sets");
        t1Req.setDueDate("2025-12-01");
        Task task1 = taskService.createTask(t1Req);

        CreateTaskRequest t2Req = new CreateTaskRequest();
        t2Req.setGroupId(group.getGroupId());
        t2Req.setCreatorUserId(owner.getUserId());
        t2Req.setTitle("Solve 5 problems");
        t2Req.setDescription("Basic combinatorics");
        t2Req.setDueDate("2025-12-05");
        Task task2 = taskService.createTask(t2Req);

        CreateTaskRequest t3Req = new CreateTaskRequest();
        t3Req.setGroupId(group.getGroupId());
        t3Req.setCreatorUserId(owner.getUserId());
        t3Req.setTitle("Prepare summary");
        t3Req.setDescription("Short summary of lecture");
        t3Req.setDueDate("2025-12-07");
        Task task3 = taskService.createTask(t3Req);

        TaskProgress t1Owner = taskService.assignUserToTask(task1.getTaskId(), owner.getUserId());
        TaskProgress t1A = taskService.assignUserToTask(task1.getTaskId(), userA.getUserId());

        TaskProgress t2A = taskService.assignUserToTask(task2.getTaskId(), userA.getUserId());
        TaskProgress t2B = taskService.assignUserToTask(task2.getTaskId(), userB.getUserId());

        TaskProgress t3B = taskService.assignUserToTask(task3.getTaskId(), userB.getUserId());

        assertEquals("OPEN", t1Owner.getStatus());
        assertEquals("OPEN", t1A.getStatus());
        assertEquals("OPEN", t2A.getStatus());
        assertEquals("OPEN", t2B.getStatus());
        assertEquals("OPEN", t3B.getStatus());

        taskService.updateUserTaskStatus(task1.getTaskId(), owner.getUserId(), "DONE");
        taskService.updateUserTaskStatus(task1.getTaskId(), userA.getUserId(), "IN_PROGRESS");

        taskService.updateUserTaskStatus(task2.getTaskId(), userA.getUserId(), "DONE");
        taskService.updateUserTaskStatus(task2.getTaskId(), userB.getUserId(), "DONE");

        taskService.updateUserTaskStatus(task3.getTaskId(), userB.getUserId(), "IN_PROGRESS");

        List<TaskProgress> progressOwner = taskProgressRepository.findByUserId(owner.getUserId());
        List<TaskProgress> progressA = taskProgressRepository.findByUserId(userA.getUserId());
        List<TaskProgress> progressB = taskProgressRepository.findByUserId(userB.getUserId());

        assertEquals(1, progressOwner.size());
        assertEquals(2, progressA.size());
        assertEquals(2, progressB.size());

        TaskProgress ownerOnT1 = taskProgressRepository
                .findByTaskIdAndUserId(task1.getTaskId(), owner.getUserId())
                .orElseThrow();
        TaskProgress userAOnT1 = taskProgressRepository
                .findByTaskIdAndUserId(task1.getTaskId(), userA.getUserId())
                .orElseThrow();
        TaskProgress userAOnT2 = taskProgressRepository
                .findByTaskIdAndUserId(task2.getTaskId(), userA.getUserId())
                .orElseThrow();
        TaskProgress userBOnT2 = taskProgressRepository
                .findByTaskIdAndUserId(task2.getTaskId(), userB.getUserId())
                .orElseThrow();
        TaskProgress userBOnT3 = taskProgressRepository
                .findByTaskIdAndUserId(task3.getTaskId(), userB.getUserId())
                .orElseThrow();

        assertEquals("DONE", ownerOnT1.getStatus());
        assertEquals("IN_PROGRESS", userAOnT1.getStatus());
        assertEquals("DONE", userAOnT2.getStatus());
        assertEquals("DONE", userBOnT2.getStatus());
        assertEquals("IN_PROGRESS", userBOnT3.getStatus());

        List<Task> tasksForGroup = taskService.getTasksForGroup(group.getGroupId());
        assertEquals(3, tasksForGroup.size());
    }
}
