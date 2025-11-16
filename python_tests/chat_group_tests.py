import requests
import time

BASE = "http://localhost:8080"


def print_response(r, method, url):
    print(f"\n=== {method} {url} ===")
    print("STATUS:", r.status_code)
    print("RAW BODY:", repr(r.text))
    print("HEADERS:", r.headers)


def health_check():
    url = f"{BASE}/actuator/health"
    r = requests.get(url)
    print_response(r, "GET", url)
    if r.status_code != 200:
        raise RuntimeError(f"Health check failed: {r.status_code} {r.text}")


def register_user(email: str, password: str = "test123", name: str | None = None):
    if name is None:
        name = email.split("@")[0]

    payload = {
        "email": email,
        "password": password,
        "name": name,
    }

    url1 = f"{BASE}/auth/register"
    r = requests.post(url1, json=payload)
    print_response(r, "POST", url1)

    if r.status_code != 200:
        url2 = f"{BASE}/api/auth/register"
        r = requests.post(url2, json=payload)
        print_response(r, "POST", url2)

    if r.status_code != 200:
        raise RuntimeError(f"Register failed: {r.status_code} {r.text}")


def login_user(email: str, password: str = "test123") -> int:
    payload = {
        "email": email,
        "password": password,
    }

    url1 = f"{BASE}/auth/login"
    r = requests.post(url1, json=payload)
    print_response(r, "POST", url1)

    if r.status_code != 200:
        url2 = f"{BASE}/api/auth/login"
        r = requests.post(url2, json=payload)
        print_response(r, "POST", url2)

    if r.status_code != 200:
        raise RuntimeError(f"Login failed: {r.status_code} {r.text}")

    text = r.text.strip()
    prefix = "login ok, userId="
    if not text.startswith(prefix):
        raise RuntimeError(f"Unexpected login response: {text}")

    return int(text[len(prefix):])


def get_participants(conversation_id: int):
    url = f"{BASE}/api/chat/conversations/{conversation_id}/participants"
    r = requests.get(url)
    print_response(r, "GET", url)
    return r


def create_group_conversation(name: str, participant_ids: list[int]) -> int:
    url = f"{BASE}/api/chat/group"
    payload = {
        "name": name,
        "participantIds": participant_ids,
    }
    r = requests.post(url, json=payload)
    print_response(r, "POST", url)
    if r.status_code != 200:
        raise RuntimeError(f"Create group conversation failed: {r.status_code} {r.text}")
    data = r.json()
    return data["conversationId"]


def test_group_conversation():
    print(">>> GROUP CONVERSATION TEST START")

    results = {
        "POST /api/chat/group": False,
        "GET /api/chat/conversations/{id}/participants (group)": False,
    }

    # 1. health
    health_check()

    # 2. реєструємо / логінимо трьох користувачів
    suffix = int(time.time())
    email1 = f"group_user1_{suffix}@test.com"
    email2 = f"group_user2_{suffix}@test.com"
    email3 = f"group_user3_{suffix}@test.com"

    print("REGISTER:", email1)
    register_user(email1)
    print("REGISTER:", email2)
    register_user(email2)
    print("REGISTER:", email3)
    register_user(email3)

    print("LOGIN:", email1)
    user1_id = login_user(email1)
    print("User1 ID:", user1_id)

    print("LOGIN:", email2)
    user2_id = login_user(email2)
    print("User2 ID:", user2_id)

    print("LOGIN:", email3)
    user3_id = login_user(email3)
    print("User3 ID:", user3_id)

    # 3. створюємо групову розмову
    conv_id = create_group_conversation(
        name="Study Group",
        participant_ids=[user1_id, user2_id, user3_id],
    )
    print("Created group conversation ID:", conv_id)
    results["POST /api/chat/group"] = True

    # 4. перевіряємо учасників
    r = get_participants(conv_id)
    if r.status_code == 200:
        data = r.json()
        ids = {p["userId"] for p in data}
        if {user1_id, user2_id, user3_id}.issubset(ids):
            results["GET /api/chat/conversations/{id}/participants (group)"] = True

    # 5. підсумок
    print("\n>>> ENDPOINT SUMMARY")
    for path, ok in results.items():
        mark = "🟢" if ok else "🔴"
        print(f"{path} {mark}")

    print(">>> GROUP CONVERSATION TEST FINISHED")


if __name__ == "__main__":
    test_group_conversation()
