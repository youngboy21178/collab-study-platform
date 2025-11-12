package app.api;

import app.dto.chat.CreateDirectConversationRequest;
import app.dto.chat.SendMessageRequest;
import app.dto.chat.MessageResponse;
import app.dto.chat.ConversationSummary; // ⬅️ ДОДАЙ ЦЕ
import app.services.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    // POST /api/chat/direct -> { "conversationId": N }
    @PostMapping("/direct")
    public ResponseEntity<Map<String, Long>> createDirectConversation(
            @RequestBody CreateDirectConversationRequest request
    ) {
        Long convId = conversationService.createDirectConversation(request);
        return ResponseEntity.ok(Map.of("conversationId", convId));
    }

    // POST /api/chat/messages
    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        MessageResponse resp = conversationService.sendMessage(request);
        return ResponseEntity.ok(resp);
    }

    // GET /api/chat/{conversationId}/messages
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long conversationId) {
        List<MessageResponse> messages = conversationService.getMessages(conversationId);
        return ResponseEntity.ok(messages);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConversationSummary>> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.getConversationsForUser(userId));
    }
}
