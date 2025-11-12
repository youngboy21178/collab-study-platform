package app.api;

import app.db.entities.Task;
import app.db.entities.TaskProgress;
import app.dto.tasks.AssignUserToTaskRequest;
import app.dto.tasks.CreateTaskRequest;
import app.dto.tasks.UpdateUserTaskStatusRequest;
import app.services.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Create new task in group
    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request);
        return ResponseEntity.ok(task);
    }

    // Get all tasks for group
    @GetMapping("/groups/{groupId}/tasks")
    public ResponseEntity<List<Task>> getTasksForGroup(@PathVariable Long groupId) {
        List<Task> tasks = taskService.getTasksForGroup(groupId);
        return ResponseEntity.ok(tasks);
    }

    // Assign user to task (create TaskProgress with OPEN status)
    @PostMapping("/tasks/{taskId}/assign")
    public ResponseEntity<TaskProgress> assignUserToTask(
            @PathVariable Long taskId,
            @RequestBody AssignUserToTaskRequest request
    ) {
        TaskProgress progress = taskService.assignUserToTask(taskId, request.getUserId());
        return ResponseEntity.ok(progress);
    }

    // Update user status for task (OPEN / IN_PROGRESS / DONE)
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

    // (optional) Get progress list for task (who is on what status)
    @GetMapping("/tasks/{taskId}/progress")
    public ResponseEntity<List<TaskProgress>> getTaskProgress(@PathVariable Long taskId) {
        List<TaskProgress> progressList = taskService.getTaskProgressForTask(taskId);
        return ResponseEntity.ok(progressList);
    }
}
