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
import app.dto.chat.ConversationDetailsResponse;
import app.dto.chat.ConversationDetailsResponse.ParticipantInfo;
import app.dto.chat.ConversationSummary;
import app.dto.chat.AddParticipantRequest;
import app.db.repositories.UserRepository;
import app.dto.chat.CreateGroupConversationRequest;
import app.db.entities.User;



import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {

        private final ConversationRepository conversationRepository;
        private final ConversationParticipantRepository participantRepository;
        private final MessageRepository messageRepository;
        private final UserRepository userRepository;

        public ConversationService(ConversationRepository conversationRepository,
                               ConversationParticipantRepository participantRepository,
                               MessageRepository messageRepository,
                               UserRepository userRepository) {
                this.conversationRepository = conversationRepository;
                this.participantRepository = participantRepository;
                this.messageRepository = messageRepository;
                this.userRepository = userRepository;
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

        @Transactional(readOnly = true)
        public ConversationDetailsResponse getConversationDetails(Long conversationId) {
                var convOpt = conversationRepository.findById(conversationId);
                if (convOpt.isEmpty()) {
                    // немає такої розмови -> хай контролер віддасть 404
                    return null;
                }

                Conversation conv = convOpt.get();

                // витягуємо учасників
                List<ConversationParticipant> participants =
                        participantRepository.findByConversationId(conversationId);

                // витягуємо повідомлення
                List<Message> messages =
                        messageRepository.findByConversationIdOrderByMessageIdAsc(conversationId);

                List<ParticipantInfo> participantInfos =
                        participants.stream()
                                .map(p -> new ParticipantInfo(
                                        p.getUserId(),
                                        null,
                                        null
                                ))
                                .toList();



                List<MessageResponse> messageDtos =
                        messages.stream()
                                .map(m -> new MessageResponse(
                                        m.getMessageId(),
                                        m.getConversationId(),
                                        m.getSenderUserId(),
                                        m.getContent()
                                ))
                                .toList();

                ConversationDetailsResponse resp = new ConversationDetailsResponse();
                resp.setConversationId(conv.getConversationId());
                resp.setType(conv.getType());
                resp.setParticipants(participantInfos);
                resp.setMessages(messageDtos);

                return resp;
        }

        @Transactional(readOnly = true)
        public List<ParticipantInfo> getParticipants(Long conversationId) {
                conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

                List<ConversationParticipant> parts =
                        participantRepository.findByConversationId(conversationId);

                return parts.stream()
                        .map(p -> {
                                User u = userRepository.findById(p.getUserId()).orElse(null);
                                String name = (u != null) ? u.getName() : null;
                                String email = (u != null) ? u.getEmail() : null;
                                return new ParticipantInfo(p.getUserId(), name, email);
                        })
                        .toList();
                        }

        @Transactional
        public void addParticipant(Long conversationId, AddParticipantRequest request) {
                conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

                if (participantRepository.existsByConversationIdAndUserId(conversationId, request.getUserId())) {
                        return; 
                }

                ConversationParticipant cp = new ConversationParticipant();
                cp.setConversationId(conversationId);
                cp.setUserId(request.getUserId());
                cp.setRole(request.getRole() != null ? request.getRole() : "PARTICIPANT");

                participantRepository.save(cp);
        }

        @Transactional
        public void removeParticipant(Long conversationId, Long userId) {
                participantRepository.deleteByConversationIdAndUserId(conversationId, userId);
        }

        @Transactional
        public Long createGroupConversation(CreateGroupConversationRequest request) {
                Conversation conv = new Conversation();
                conv.setType("GROUP");   // на DIRECT ми вже ставимо "DIRECT"
                Conversation savedConv = conversationRepository.save(conv);

                if (request.getParticipantIds() != null) {
                        for (Long userId : request.getParticipantIds()) {
                        ConversationParticipant cp = new ConversationParticipant();
                        cp.setConversationId(savedConv.getConversationId());
                        cp.setUserId(userId);
                        cp.setRole("PARTICIPANT");
                        participantRepository.save(cp);
                        }
                }

                return savedConv.getConversationId();
        }

        @Transactional
        public MessageResponse updateMessage(Long messageId, String newContent) {
                Message m = messageRepository.findById(messageId)
                        .orElseThrow(() -> new IllegalArgumentException("Message not found"));

                m.setContent(newContent);
                Message saved = messageRepository.save(m);

                return new MessageResponse(
                        saved.getMessageId(),
                        saved.getConversationId(),
                        saved.getSenderUserId(),
                        saved.getContent()
                );
        }

        @Transactional
        public void deleteMessage(Long messageId) {
                if (!messageRepository.existsById(messageId)) {
                        throw new IllegalArgumentException("Message not found");
                }
                messageRepository.deleteById(messageId);
        }
}