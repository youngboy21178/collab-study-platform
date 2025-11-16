package app.dto.chat;

import java.util.List;

public class CreateGroupConversationRequest {

    private String name;               // поки можна не використовувати
    private List<Long> participantIds;

    public CreateGroupConversationRequest() {
    }

    public CreateGroupConversationRequest(String name, List<Long> participantIds) {
        this.name = name;
        this.participantIds = participantIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }
}
