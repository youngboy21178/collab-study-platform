package app.api;

import app.db.entities.Task;
import app.db.entities.TaskProgress;
import app.dto.tasks.AssignUserToTaskRequest;
import app.dto.tasks.CreateTaskRequest;
import app.dto.tasks.UpdateUserTaskStatusRequest;
import app.services.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <--- ІМПОРТ 1
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;
    private final SimpMessagingTemplate messagingTemplate; // <--- ЗМІННА 2

    // Додаємо messagingTemplate в конструктор
    public TaskController(TaskService taskService, SimpMessagingTemplate messagingTemplate) {
        this.taskService = taskService;
        this.messagingTemplate = messagingTemplate;
    }

    // Create new task in group
    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request);

        messagingTemplate.convertAndSend("/topic/groups/" + request.getGroupId() + "/tasks", task);

        return ResponseEntity.ok(task);
    }

    // Get all tasks for group
    @GetMapping("/groups/{groupId}/tasks")
    public ResponseEntity<List<Task>> getTasksForGroup(@PathVariable Long groupId) {
        List<Task> tasks = taskService.getTasksForGroup(groupId);
        return ResponseEntity.ok(tasks);
    }

    // Assign user to task
    @PostMapping("/tasks/{taskId}/assign")
    public ResponseEntity<TaskProgress> assignUserToTask(
            @PathVariable Long taskId,
            @RequestBody AssignUserToTaskRequest request
    ) {
        TaskProgress progress = taskService.assignUserToTask(taskId, request.getUserId());
        return ResponseEntity.ok(progress);
    }

    @PatchMapping("/tasks/{taskId}/progress")
    public ResponseEntity<TaskProgress> updateUserTaskStatus(
            @PathVariable Long taskId,
            @RequestBody UpdateUserTaskStatusRequest request
    ) {
        TaskProgress progress = taskService.updateUserTaskStatus(
                taskId,
                request.getUserId(),
                request.getStatus()
        );

        return ResponseEntity.ok(progress);
    }

    @GetMapping("/tasks/{taskId}/progress")
    public ResponseEntity<List<TaskProgress>> getTaskProgress(@PathVariable Long taskId) {
        List<TaskProgress> progressList = taskService.getTaskProgressForTask(taskId);
        return ResponseEntity.ok(progressList);
    }
}