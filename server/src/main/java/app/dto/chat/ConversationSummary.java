package app.dto.chat;

public class ConversationSummary {
    private Long conversationId;
    private String type;

    public ConversationSummary() {
    }

    public ConversationSummary(Long conversationId, String type) {
        this.conversationId = conversationId;
        this.type = type;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
