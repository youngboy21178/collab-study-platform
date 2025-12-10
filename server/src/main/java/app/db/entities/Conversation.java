package app.db.entities;

import app.db.converters.LocalDateTimeEpochMillisConverter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "created_at", nullable = false)
    @Convert(converter = LocalDateTimeEpochMillisConverter.class)
    private LocalDateTime createdAt;

    // 🔹 participants – унінаправлений звʼязок по FK conversation_id
    @OneToMany
    @JoinColumn(name = "conversation_id")
    private Set<ConversationParticipant> participants = new HashSet<>();

    // 🔹 messages – так само, унінаправлений звʼязок
    @OneToMany
    @JoinColumn(name = "conversation_id")
    private Set<Message> messages = new HashSet<>();

    public Conversation() {
    }

    public Conversation(String type, Long groupId) {
        this.type = type;
        this.groupId = groupId;
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // геттери / сеттери ...

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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<ConversationParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<ConversationParticipant> participants) {
        this.participants = participants;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }
}
