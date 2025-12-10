package app.dto.chat;

public class MessageResponse {
    private Long messageId;
    private Long conversationId;
    private Long senderUserId;
    private String senderName; // Ім'я відправника
    private String content;
    private String createdAt;  // Час створення

    // --- ОБОВ'ЯЗКОВО: Порожній конструктор ---
    public MessageResponse() {
    }

    public MessageResponse(Long messageId, Long conversationId, Long senderUserId, String senderName, String content, String createdAt) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderUserId = senderUserId;
        this.senderName = senderName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}