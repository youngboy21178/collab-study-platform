package app.db.entities;

import jakarta.persistence.*;

@Entity
@Table(
    name = "conversation_participants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "user_id"})
)
public class ConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role", nullable = false)
    private String role; // PARTICIPANT, ADMIN...

    public Long getParticipantId() {
        return participantId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}
