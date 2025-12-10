package app.api;

import app.dto.chat.CreateDirectConversationRequest;
import app.dto.chat.ConversationDetailsResponse;
import app.dto.chat.SendMessageRequest;
import app.dto.chat.MessageResponse;
import app.dto.chat.ConversationSummary;
import app.services.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping; // <--- ВАЖЛИВО
import org.springframework.messaging.handler.annotation.Payload;       // <--- ВАЖЛИВО
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import app.dto.chat.CreateConversationResponse;
import app.dto.chat.AddParticipantRequest;
import app.dto.chat.CreateGroupConversationRequest;
import app.dto.chat.UpdateMessageRequest;
import app.dto.chat.ConversationDetailsResponse.ParticipantInfo;
import app.dto.chat.UpdateReadReceiptRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ConversationController {

    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversationController(ConversationService conversationService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.conversationService = conversationService;
        this.messagingTemplate = messagingTemplate;
    }

    // --- ОСЬ ЦЬОГО МЕТОДУ НЕ ВИСТАЧАЛО ---
    // Цей метод обробляє повідомлення, які приходять через WebSocket
    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload SendMessageRequest request) {
        // 1. Зберігаємо повідомлення в БД
        MessageResponse resp = conversationService.sendMessage(request);

        // 2. Розсилаємо всім підписникам
        messagingTemplate.convertAndSend("/topic/conversations/" + request.getConversationId(), resp);
    }
    // --------------------------------------

    // POST /api/chat/messages (REST спосіб, залишаємо про всяк випадок)
    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        MessageResponse resp = conversationService.sendMessage(request);
        messagingTemplate.convertAndSend("/topic/conversations/" + request.getConversationId(), resp);
        return ResponseEntity.ok(resp);
    }

    // ... (Решта методів залишаються без змін) ...

    @PostMapping("/direct")
    public ResponseEntity<Map<String, Long>> createDirectConversation(
            @RequestBody CreateDirectConversationRequest request
    ) {
        Long convId = conversationService.createDirectConversation(request);
        return ResponseEntity.ok(Map.of("conversationId", convId));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(value = "afterId", required = false) Long afterId,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        List<MessageResponse> messages = conversationService.getMessages(conversationId, afterId, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConversationSummary>> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.getConversationsForUser(userId));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationDetailsResponse> getConversationDetails(
            @PathVariable Long conversationId
    ) {
        ConversationDetailsResponse details = conversationService.getConversationDetails(conversationId);
        if (details == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(details);
    }

    @GetMapping("/conversations/{conversationId}/participants")
    public ResponseEntity<List<ParticipantInfo>> getParticipants(
            @PathVariable Long conversationId
    ) {
        List<ParticipantInfo> participants = conversationService.getParticipants(conversationId);
        return ResponseEntity.ok(participants);
    }

    @PostMapping("/conversations/{conversationId}/participants")
    public ResponseEntity<Void> addParticipant(
            @PathVariable Long conversationId,
            @RequestBody AddParticipantRequest request
    ) {
        conversationService.addParticipant(conversationId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/conversations/{conversationId}/participants/{userId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Long conversationId,
            @PathVariable Long userId
    ) {
        conversationService.removeParticipant(conversationId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/group")
    public ResponseEntity<CreateConversationResponse> createGroupConversation(
            @RequestBody CreateGroupConversationRequest request
    ) {
        Long convId = conversationService.createGroupConversation(request);
        return ResponseEntity.ok(new CreateConversationResponse(convId));
    }

    @PatchMapping("/messages/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable Long messageId,
            @RequestBody UpdateMessageRequest request
    ) {
        MessageResponse resp = conversationService.updateMessage(messageId, request.getContent());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        conversationService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversations/{conversationId}/read-receipts")
    public ResponseEntity<Void> updateReadReceipt(
            @PathVariable Long conversationId,
            @RequestBody UpdateReadReceiptRequest request
    ) {
        conversationService.updateReadReceipt(
                conversationId,
                request.getUserId(),
                request.getLastReadMessageId()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversations/{conversationId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long conversationId,
            @RequestParam("userId") Long userId
    ) {
        long count = conversationService.getUnreadCount(conversationId, userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/conversations/of-user/{userId}")
    public ResponseEntity<List<ConversationSummary>> getConversationsForUser(
            @PathVariable Long userId
    ) {
        List<ConversationSummary> conversations = conversationService.getConversationsForUser(userId);
        return ResponseEntity.ok(conversations);
    }
}