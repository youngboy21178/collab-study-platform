package app.dto.chat;

public class MessageResponse {
    private Long messageId;
    private Long conversationId;
    private Long senderUserId;
    private String content;

    public MessageResponse(Long messageId, Long conversationId, Long senderUserId, String content) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderUserId = senderUserId;
        this.content = content;
    }

    public Long getMessageId() {
        return messageId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public String getContent() {
        return content;
    }
}
