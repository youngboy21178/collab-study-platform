package app.db.repositories;

import app.db.entities.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findTopByConversationIdOrderByMessageIdDesc(Long conversationId);

    long countByConversationId(Long conversationId);
    long countByConversationIdAndMessageIdGreaterThan(Long conversationId, Long messageId);

    // старий метод – використовується в getConversationDetails(...)
    List<Message> findByConversationIdOrderByMessageIdAsc(Long conversationId);

    // 1. Якщо afterId немає (початкове завантаження з лімітом)
    List<Message> findByConversationIdOrderByMessageIdAsc(Long conversationId, Pageable pageable);

    // 2. Якщо afterId є (інкрементальне завантаження)
    List<Message> findByConversationIdAndMessageIdGreaterThanOrderByMessageIdAsc(
            Long conversationId,
            Long afterId,
            Pageable pageable
    );
}
