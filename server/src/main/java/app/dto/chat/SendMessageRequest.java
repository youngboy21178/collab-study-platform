package app.dto.chat;

public class SendMessageRequest {
    private Long conversationId;
    private Long senderUserId;
    private String content;


    public SendMessageRequest() {
    }


    // Ваш старий код (геттери/сеттери)
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}