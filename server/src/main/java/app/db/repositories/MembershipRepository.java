package app.db.repositories;

import app.db.entities.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // <--- Не забудь імпорт

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByGroupId(Long groupId);

    List<Membership> findByUserId(Long userId);

    // --- НОВИЙ МЕТОД ---
    // Знайти запис про участь конкретного юзера в конкретній групі
    Optional<Membership> findByGroupIdAndUserId(Long groupId, Long userId);
}