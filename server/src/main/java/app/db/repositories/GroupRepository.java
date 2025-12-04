package app.db.repositories;

import app.db.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    // --- ДОДАНО ---
    List<Group> findByOwnerUserId(Long ownerUserId);
}