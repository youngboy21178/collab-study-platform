import requests
import time
from datetime import datetime

BASE = "http://localhost:8080"

# We try both styles: "/auth" and "/api/auth"
AUTH_PREFIXES = ["/auth", "/api/auth"]

# Adjust this if your ConversationController has different mapping
# e.g. @RequestMapping("/api/chat") or @RequestMapping("/api/conversations")
CHAT_PREFIX = "/api/chat"

def debug_print(title, r: requests.Response):
    print(f"\n=== {title} ===")
    print("STATUS:", r.status_code)
    print("RAW BODY:", repr(r.text))
    print("HEADERS:", dict(r.headers))

def try_post_with_prefixes(prefixes, subpath, json_payload):
    """
    Try POST on several possible prefixes, return first non-404 response.
    If all are 404, raise RuntimeError with last response.
    """
    last_resp = None
    for pref in prefixes:
        url = f"{BASE}{pref}{subpath}"
        r = requests.post(url, json=json_payload, timeout=5)
        debug_print(f"POST {url}", r)
        if r.status_code != 404:
            return r
        last_resp = r
    raise RuntimeError(
        f"All prefixes gave 404 for {subpath}. "
        f"Last response: {last_resp.status_code} {last_resp.text if last_resp else 'NO RESPONSE'}"
    )

def register_user(name, email, password):
    payload = {"name": name, "email": email, "password": password}
    r = try_post_with_prefixes(AUTH_PREFIXES, "/register", payload)
    if r.status_code != 200:
        raise RuntimeError(f"Register failed for {email}: {r.status_code} {r.text}")

def login_user(email, password):
    payload = {"email": email, "password": password}
    r = try_post_with_prefixes(AUTH_PREFIXES, "/login", payload)
    if r.status_code != 200:
        raise RuntimeError(f"Login failed for {email}: {r.status_code} {r.text}")

    # expected response: "login ok, userId=5"
    body = r.text.strip()
    if "userId=" not in body:
        raise RuntimeError(f"Unexpected login response for {email}: '{body}'")
    user_id_part = body.split("userId=")[1]
    user_id = int(user_id_part)
    return user_id

def create_direct_conversation(user1_id, user2_id):
    """
    Adjust this to match your ConversationController mapping.

    Expected Spring endpoint something like:
    @PostMapping("/direct")
    public Conversation createDirectConversation(@RequestBody CreateConversationRequest request)

    where request has: user1Id, user2Id
    """
    url = f"{BASE}{CHAT_PREFIX}/direct"
    payload = {
        "user1Id": user1_id,
        "user2Id": user2_id
    }
    r = requests.post(url, json=payload, timeout=5)
    debug_print(f"POST {url}", r)
    if r.status_code != 200:
        raise RuntimeError(f"Create direct conversation failed: {r.status_code} {r.text}")
    conv = r.json()
    if "conversationId" not in conv:
        raise RuntimeError(f"No 'conversationId' in response: {conv}")
    return conv["conversationId"]

def send_message(conversation_id, sender_user_id, content):
    """
    Expected Spring endpoint something like:
    @PostMapping("/messages")
    public MessageResponse sendMessage(@RequestBody SendMessageRequest request)

    where request has: conversationId, senderUserId, content
    """
    url = f"{BASE}{CHAT_PREFIX}/messages"
    payload = {
        "conversationId": conversation_id,
        "senderUserId": sender_user_id,
        "content": content
    }
    r = requests.post(url, json=payload, timeout=5)
    debug_print(f"POST {url}", r)
    if r.status_code != 200:
        raise RuntimeError(f"Send message failed: {r.status_code} {r.text}")
    return r.json()

def get_messages(conversation_id):
    """
    Expected Spring endpoint something like:
    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> getMessages(@PathVariable Long conversationId)
    """
    url = f"{BASE}{CHAT_PREFIX}/{conversation_id}/messages"
    r = requests.get(url, timeout=5)
    debug_print(f"GET {url}", r)
    if r.status_code != 200:
        raise RuntimeError(f"Get messages failed: {r.status_code} {r.text}")
    return r.json()

def get_user_conversations(user_id):
    """
    Expected Spring endpoint something like:
    @GetMapping("/user/{userId}")
    public List<Conversation> getUserConversations(@PathVariable Long userId)
    """
    url = f"{BASE}{CHAT_PREFIX}/user/{user_id}"
    r = requests.get(url, timeout=5)
    debug_print(f"GET {url}", r)
    if r.status_code != 200:
        raise RuntimeError(f"Get user conversations failed: {r.status_code} {r.text}")
    return r.json()

def test_chat_flow():
    print(">>> CHAT TEST START")

    # 1. Optional health check (might be 404 – that's fine)
    try:
        r = requests.get(f"{BASE}/actuator/health", timeout=3)
        debug_print("HEALTH", r)
    except Exception as e:
        print("HEALTHCHECK error:", e)

    # 2. Register two users
    timestamp = int(time.time())
    email1 = f"user1_{timestamp}@test.com"
    email2 = f"user2_{timestamp}@test.com"
    password = "test123"

    print("REGISTER:", email1)
    register_user("User One", email1, password)

    print("REGISTER:", email2)
    register_user("User Two", email2, password)

    # 3. Login both, parse user IDs
    print("LOGIN:", email1)
    user1_id = login_user(email1, password)
    print("User1 ID:", user1_id)

    print("LOGIN:", email2)
    user2_id = login_user(email2, password)
    print("User2 ID:", user2_id)

    # 4. Create direct conversation between them
    conv_id = create_direct_conversation(user1_id, user2_id)
    print("Created conversation ID:", conv_id)

    # 5. Send messages both ways
    msg1 = send_message(conv_id, user1_id, "Hi from user1!")
    print("Message1:", msg1)

    msg2 = send_message(conv_id, user2_id, "Hello user1, this is user2.")
    print("Message2:", msg2)

    # 6. Get all messages in conversation
    messages = get_messages(conv_id)
    print("Messages in conversation:")
    for m in messages:
        print("  ->", m)

    # 7. Get conversations for user1 and user2
    convs1 = get_user_conversations(user1_id)
    convs2 = get_user_conversations(user2_id)

    print("User1 conversations:", convs1)
    print("User2 conversations:", convs2)

    print(">>> CHAT TEST FINISHED OK")

if __name__ == "__main__":
    test_chat_flow()
