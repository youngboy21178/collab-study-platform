package app.db.repositories;

import app.db.entities.TaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskProgressRepository extends JpaRepository<TaskProgress, Long> {

    List<TaskProgress> findByTaskId(Long taskId);

    List<TaskProgress> findByUserId(Long userId);

    Optional<TaskProgress> findByTaskIdAndUserId(Long taskId, Long userId);
}
