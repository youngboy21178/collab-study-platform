package app.db.repositories;

import app.db.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByMessageIdAsc(Long conversationId);

    Optional<Message> findTopByConversationIdOrderByMessageIdDesc(Long conversationId);
}
