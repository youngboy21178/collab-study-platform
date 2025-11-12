package app.services;

import app.db.entities.Task;
import app.db.entities.TaskProgress;
import app.db.entities.TaskStatus;
import app.db.repositories.MembershipRepository;
import app.db.repositories.TaskProgressRepository;
import app.db.repositories.TaskRepository;
import app.dto.tasks.CreateTaskRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskProgressRepository taskProgressRepository;
    private final MembershipRepository membershipRepository;
    private final GroupService groupService;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository,
                       TaskProgressRepository taskProgressRepository,
                       MembershipRepository membershipRepository,
                       GroupService groupService,
                       UserService userService) {
        this.taskRepository = taskRepository;
        this.taskProgressRepository = taskProgressRepository;
        this.membershipRepository = membershipRepository;
        this.groupService = groupService;
        this.userService = userService;
    }

    public Task createTask(CreateTaskRequest request) {
        groupService.getGroupById(request.getGroupId());
        userService.getUser(request.getCreatorUserId());

        Task task = new Task();
        task.setGroupId(request.getGroupId());
        task.setCreatorUserId(request.getCreatorUserId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.OPEN.name());

        if (request.getDueDate() != null && !request.getDueDate().isBlank()) {
            LocalDate due = LocalDate.parse(request.getDueDate());
            task.setDueDate(due.atStartOfDay());
        }

        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    public List<Task> getTasksForGroup(Long groupId) {
        return taskRepository.findByGroupId(groupId);
    }

    public Task updateStatus(Long taskId, String newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        TaskStatus status = TaskStatus.valueOf(newStatus);
        task.setStatus(status.name());
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Task not found");
        }
        taskRepository.deleteById(taskId);
    }

    public TaskProgress assignUserToTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        userService.getUser(userId);

        boolean isMember = membershipRepository
                .findByGroupId(task.getGroupId())
                .stream()
                .anyMatch(m -> m.getUserId().equals(userId));

        if (!isMember) {
            throw new IllegalArgumentException("User is not a member of the group");
        }

        return taskProgressRepository.findByTaskIdAndUserId(taskId, userId)
                .orElseGet(() -> {
                    TaskProgress p = new TaskProgress();
                    p.setTaskId(taskId);
                    p.setUserId(userId);
                    p.setStatus(TaskStatus.OPEN.name());
                    p.setUpdatedAt(LocalDateTime.now());
                    p.setCompletedAt(null);
                    return taskProgressRepository.save(p);
                });
    }

    public TaskProgress updateUserTaskStatus(Long taskId, Long userId, String newStatus) {
        TaskProgress progress = taskProgressRepository
                .findByTaskIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not assigned to this task"));

        TaskStatus status = TaskStatus.valueOf(newStatus);
        progress.setStatus(status.name());
        progress.setUpdatedAt(LocalDateTime.now());

        if (status == TaskStatus.DONE) {
            progress.setCompletedAt(LocalDateTime.now());
        } else {
            progress.setCompletedAt(null);
        }

        return taskProgressRepository.save(progress);
    }
    
    public List<TaskProgress> getTaskProgressForTask(Long taskId) {
        return taskProgressRepository.findByTaskId(taskId);
    }
}
