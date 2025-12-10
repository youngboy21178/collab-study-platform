package app.db.repositories;

import app.db.entities.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // Знайти ресурси конкретної групи
    List<Resource> findByGroup_GroupIdOrderByUploadedAtDesc(Long groupId);

    // SQL запит для вкладки "Всі ресурси"
    @Query("SELECT r FROM Resource r " +
           "WHERE r.group.groupId IN " +
           "(SELECT m.groupId FROM Membership m WHERE m.userId = :userId) " +
           "ORDER BY r.uploadedAt DESC")
    List<Resource> findAllAccessibleResources(@Param("userId") Long userId);
}