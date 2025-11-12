package app.services;

import app.db.entities.Conversation;
import app.db.entities.ConversationParticipant;
import app.db.entities.Message;
import app.db.repositories.ConversationParticipantRepository;
import app.db.repositories.ConversationRepository;
import app.db.repositories.MessageRepository;
import app.dto.chat.CreateDirectConversationRequest;
import app.dto.chat.MessageResponse;
import app.dto.chat.SendMessageRequest;
import app.dto.chat.ConversationSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ConversationParticipantRepository participantRepository,
                               MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Long createDirectConversation(CreateDirectConversationRequest request) {
        Conversation conv = new Conversation();
        conv.setType("DIRECT");
        Conversation savedConv = conversationRepository.save(conv);

        ConversationParticipant p1 = new ConversationParticipant();
        p1.setConversationId(savedConv.getConversationId());
        p1.setUserId(request.getUser1Id());
        p1.setRole("PARTICIPANT");

        ConversationParticipant p2 = new ConversationParticipant();
        p2.setConversationId(savedConv.getConversationId());
        p2.setUserId(request.getUser2Id());
        p2.setRole("PARTICIPANT");

        participantRepository.save(p1);
        participantRepository.save(p2);

        return savedConv.getConversationId();
    }

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        // Optional: ensure conversation exists
        conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Message m = new Message();
        m.setConversationId(request.getConversationId());
        m.setSenderUserId(request.getSenderUserId());
        m.setContent(request.getContent());

        Message saved = messageRepository.save(m);

        return new MessageResponse(
                saved.getMessageId(),
                saved.getConversationId(),
                saved.getSenderUserId(),
                saved.getContent()
        );
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByMessageIdAsc(conversationId);
        return messages.stream()
                .map(m -> new MessageResponse(
                        m.getMessageId(),
                        m.getConversationId(),
                        m.getSenderUserId(),
                        m.getContent()
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<ConversationSummary> getConversationsForUser(Long userId) {
        // 1) Витягуємо всі conversationId користувача одним запитом
        List<Long> ids = participantRepository.findConversationIdsByUserId(userId);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        // 2) Тягнемо всі розмови одним батчем (JpaRepository вже має findAllById)
        List<Conversation> convs = conversationRepository.findAllById(ids);
        // 3) Мапимо у DTO
        return convs.stream()
                .map(c -> new ConversationSummary(c.getConversationId(), c.getType()))
                .toList();
    }
}
