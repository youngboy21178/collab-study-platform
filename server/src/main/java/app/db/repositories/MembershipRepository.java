package app.db.repositories;

import app.db.entities.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    List<Membership> findByGroupId(Long groupId);
}
