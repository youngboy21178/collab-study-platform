package app.services;

import app.db.entities.Task;
import app.db.entities.TaskProgress;
import app.db.entities.TaskStatus;
import app.db.repositories.MembershipRepository;
import app.db.repositories.TaskProgressRepository;
import app.db.repositories.TaskRepository;
import app.dto.tasks.CreateTaskRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskProgressRepository taskProgressRepository;
    private final MembershipRepository membershipRepository;
    private final GroupService groupService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public TaskService(TaskRepository taskRepository,
                       TaskProgressRepository taskProgressRepository,
                       MembershipRepository membershipRepository,
                       GroupService groupService,
                       UserService userService,
                       SimpMessagingTemplate messagingTemplate) {
        this.taskRepository = taskRepository;
        this.taskProgressRepository = taskProgressRepository;
        this.membershipRepository = membershipRepository;
        this.groupService = groupService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
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
            task.setDueDate(request.getDueDate());
        }

        String now = Instant.now().toString();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        Task savedTask = taskRepository.save(task);
        notifyGroup(savedTask); // Сповіщаємо про створення
        return savedTask;
    }

    public List<Task> getTasksForGroup(Long groupId) {
        return taskRepository.findByGroupId(groupId);
    }

    public Task updateStatus(Long taskId, String newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        TaskStatus status = TaskStatus.valueOf(newStatus);
        task.setStatus(status.name());
        task.setUpdatedAt(Instant.now().toString());

        Task saved = taskRepository.save(task);
        notifyGroup(saved); // Сповіщаємо про глобальну зміну
        return saved;
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

        TaskProgress progress = taskProgressRepository.findByTaskIdAndUserId(taskId, userId)
                .orElseGet(() -> {
                    TaskProgress p = new TaskProgress();
                    p.setTaskId(taskId);
                    p.setUserId(userId);
                    p.setStatus(TaskStatus.OPEN.name());
                    p.setUpdatedAt(Instant.now().toString());
                    p.setCompletedAt(null);
                    return taskProgressRepository.save(p);
                });

        // --- ВАЖЛИВО: СПОВІЩЕННЯ ПРИ ПРИЗНАЧЕННІ ---
        notifyGroup(task);

        return progress;
    }

    public TaskProgress updateUserTaskStatus(Long taskId, Long userId, String newStatus) {
        TaskProgress progress = taskProgressRepository
                .findByTaskIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not assigned to this task"));

        TaskStatus status = TaskStatus.valueOf(newStatus);
        progress.setStatus(status.name());
        progress.setUpdatedAt(Instant.now().toString());

        if (status == TaskStatus.DONE) {
            progress.setCompletedAt(Instant.now().toString());
        } else {
            progress.setCompletedAt(null);
        }

        TaskProgress saved = taskProgressRepository.save(progress);

        // --- ВАЖЛИВО: СПОВІЩЕННЯ ПРИ ЗМІНІ СТАТУСУ ---
        // Знаходимо сам таск, щоб відправити його клієнтам як сигнал оновлення
        taskRepository.findById(taskId).ifPresent(this::notifyGroup);

        return saved;
    }

    public List<TaskProgress> getTaskProgressForTask(Long taskId) {
        return taskProgressRepository.findByTaskId(taskId);
    }

    private void notifyGroup(Task task) {
        String destination = "/topic/groups/" + task.getGroupId() + "/tasks";
        messagingTemplate.convertAndSend(destination, task);
    }
}