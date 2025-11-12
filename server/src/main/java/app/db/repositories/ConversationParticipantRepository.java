package app.db.repositories;

import app.db.entities.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;          // <-- ДОДАНО
import org.springframework.data.repository.query.Param;   // <-- ДОДАНО

import java.util.List;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    // Якщо в тебе вже було щось таке – залишай
    List<ConversationParticipant> findByUserId(Long userId);

    @Query("select p.conversationId from ConversationParticipant p where p.userId = :userId")
    List<Long> findConversationIdsByUserId(@Param("userId") Long userId);
}
