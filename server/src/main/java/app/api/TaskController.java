package app.api;

import app.db.entities.Task;
import app.db.entities.TaskProgress;
import app.dto.tasks.CreateTaskRequest;
import app.services.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Task>> getTasksForGroup(@PathVariable Long groupId) {
        List<Task> tasks = taskService.getTasksForGroup(groupId);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Task> updateStatus(
            @PathVariable Long taskId,
            @RequestParam String status
    ) {
        Task task = taskService.updateStatus(taskId, status);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<TaskProgress> assignUser(
            @PathVariable Long taskId,
            @RequestParam Long userId
    ) {
        TaskProgress progress = taskService.assignUserToTask(taskId, userId);
        return ResponseEntity.ok(progress);
    }

    @PatchMapping("/{taskId}/status/user/{userId}")
    public ResponseEntity<TaskProgress> updateUserStatus(
            @PathVariable Long taskId,
            @PathVariable Long userId,
            @RequestParam String status
    ) {
        TaskProgress progress = taskService.updateUserTaskStatus(taskId, userId, status);
        return ResponseEntity.ok(progress);
    }
}
