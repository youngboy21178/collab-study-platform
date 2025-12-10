package app.db.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    // Явно вказуємо назву колонки, щоб уникнути DuplicateMappingException
    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "sender_user_id")
    private Long senderUserId;

    @Column(name = "content", length = 5000)
    private String content;

    @Column(name = "created_at")
    private String createdAt;

    public Message() {
    }

    // Геттери та Сеттери
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}