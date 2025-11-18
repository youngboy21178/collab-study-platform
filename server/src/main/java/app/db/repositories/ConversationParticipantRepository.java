package app.db.repositories;

import java.util.Optional;
import app.db.entities.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;          
import org.springframework.data.repository.query.Param;   

import java.util.List;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    @Query("select p.conversationId from ConversationParticipant p where p.userId = :userId")
    List<Long> findConversationIdsByUserId(@Param("userId") Long userId);
    List<ConversationParticipant> findByUserId(Long userId);
    List<ConversationParticipant> findByConversationId(Long conversationId);
    Optional<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);

    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
    void deleteByConversationIdAndUserId(Long conversationId, Long userId);
}
