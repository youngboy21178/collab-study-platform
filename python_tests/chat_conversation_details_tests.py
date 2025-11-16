import time
import requests

BASE_URL = "http://localhost:8080"
from chat_chatflow_tests import (
    BASE,
    CHAT_PREFIX,
    AUTH_PREFIXES,
    debug_print,
    register_user,
    login_user,
    create_direct_conversation,
    send_message,
)


def get_conversation_details(conv_id: int) -> dict|None:
    url = f"{BASE_URL}/api/chat/conversations/{conv_id}"
    print(f"\n=== GET {url} ===")
    r = requests.get(url)
    print("STATUS:", r.status_code)
    print("RAW BODY:", repr(r.text))
    print("HEADERS:", r.headers)

    if r.status_code == 200:
        return r.json()
    elif r.status_code == 404:
        # очікуваний кейс для неіснуючої розмови
        print(f"Conversation {conv_id} not found (404) – OK")
        return None
    else:
        raise RuntimeError(
            f"Get conversation details failed: {r.status_code} {r.text}"
        )


def test_conversation_details():
    print(">>> CONVERSATION DETAILS TEST START")

    # 1. (опційно) healthcheck
    try:
        r = requests.get(f"{BASE}/actuator/health", timeout=3)
        debug_print("HEALTH", r)
    except Exception as e:
        print("HEALTHCHECK error:", e)

    # 2. Реєстрація двох користувачів
    ts = int(time.time())
    email1 = f"user1_{ts}@test.com"
    email2 = f"user2_{ts}@test.com"
    password = "test123"

    print("REGISTER:", email1)
    register_user("User One", email1, password)

    print("REGISTER:", email2)
    register_user("User Two", email2, password)

    # 3. Логін – отримуємо userId
    print("LOGIN:", email1)
    user1_id = login_user(email1, password)
    print("User1 ID:", user1_id)

    print("LOGIN:", email2)
    user2_id = login_user(email2, password)
    print("User2 ID:", user2_id)

    # 4. Створюємо DIRECT-діалог
    conv_id = create_direct_conversation(user1_id, user2_id)
    print("Created conversation ID:", conv_id)

    # 5. Шлемо пару повідомлень (щоб у деталях була історія)
    send_message(conv_id, user1_id, "Hi from user1!")
    send_message(conv_id, user2_id, "Hello user1, this is user2.")

    # 6. Тестуємо сам endpoint деталей
    details = get_conversation_details(conv_id)
    print("Conversation details:", details)

    # Мінімальні перевірки форми відповіді
    assert details.get("conversationId") == conv_id, "conversationId mismatch"
    assert "type" in details, "missing 'type'"
    # необов’язково, але корисно:
    # assert "participants" in details, "missing 'participants'"

    # 1) існуюча бесіда
    details = get_conversation_details(conv_id)
    print(f"DETAILS {details}")
    assert details is not None, "Details for existing conversation must not be None"
    assert details["conversationId"] == conv_id
    assert details["type"] == "DIRECT"
    assert len(details["participants"]) == 2
    assert len(details["messages"]) == 2

    # 2) неіснуюча бесіда -> 404
    non_existing_id = conv_id + 999
    not_found = get_conversation_details(non_existing_id)
    assert not_found is None, "For non-existing conversation we expect None (404)"

    print(">>> CONVERSATION DETAILS TEST FINISHED OK")


if __name__ == "__main__":
    test_conversation_details()
