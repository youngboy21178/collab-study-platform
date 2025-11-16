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

    # старий шлях (404, але лишимо як в інших тестах)
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

    # "login ok, userId=1"
    text = r.text.strip()
    prefix = "login ok, userId="
    if not text.startswith(prefix):
        raise RuntimeError(f"Unexpected login response: {text}")

    return int(text[len(prefix):])


def create_direct_conversation(user1_id: int, user2_id: int) -> int:
    url = f"{BASE}/api/chat/direct"
    payload = {
        "user1Id": user1_id,
        "user2Id": user2_id,
    }
    r = requests.post(url, json=payload)
    print_response(r, "POST", url)
    if r.status_code != 200:
        raise RuntimeError(f"Create direct conversation failed: {r.status_code} {r.text}")
    data = r.json()
    return data["conversationId"]


def add_participant(conversation_id: int, user_id: int):
    url = f"{BASE}/api/chat/conversations/{conversation_id}/participants"
    # якщо твій DTO інший – просто підправ тут payload
    payload = {"userId": user_id}
    r = requests.post(url, json=payload)
    print_response(r, "POST", url)
    return r


def get_participants(conversation_id: int):
    url = f"{BASE}/api/chat/conversations/{conversation_id}/participants"
    r = requests.get(url)
    print_response(r, "GET", url)
    return r


def delete_participant(conversation_id: int, user_id: int):
    url = f"{BASE}/api/chat/conversations/{conversation_id}/participants/{user_id}"
    r = requests.delete(url)
    print_response(r, "DELETE", url)
    return r


def test_participants_flow():
    print(">>> PARTICIPANTS TEST START")

    # для фінального репорту
    results = {
        "GET /api/chat/conversations/{id}/participants": False,
        "POST /api/chat/conversations/{id}/participants": False,
        "DELETE /api/chat/conversations/{id}/participants/{userId}": False,
    }

    # 1. Health
    health_check()

    # 2. Реєстрація / логін трьох юзерів
    suffix = int(time.time())
    email1 = f"user1_{suffix}@test.com"
    email2 = f"user2_{suffix}@test.com"
    email3 = f"user3_{suffix}@test.com"

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

    # 3. Створюємо DIRECT-розмову між user1 та user2
    conv_id = create_direct_conversation(user1_id, user2_id)
    print("Created conversation ID:", conv_id)

    # 4. Перевіряємо GET participants – мають бути user1 та user2
    r = get_participants(conv_id)
    if r.status_code == 200:
        data = r.json()
        user_ids = {p["userId"] for p in data}
        if user1_id in user_ids and user2_id in user_ids:
            results["GET /api/chat/conversations/{id}/participants"] = True

    # 5. Додаємо user3 у розмову
    r = add_participant(conv_id, user3_id)
    if r.status_code in (200, 201, 204):
        # ще раз читаємо учасників
        r2 = get_participants(conv_id)
        if r2.status_code == 200:
            data = r2.json()
            user_ids = {p["userId"] for p in data}
            if user3_id in user_ids:
                results["POST /api/chat/conversations/{id}/participants"] = True

    # 6. Видаляємо user3 з розмови
    r = delete_participant(conv_id, user3_id)
    if r.status_code in (200, 204):
        r2 = get_participants(conv_id)
        if r2.status_code == 200:
            data = r2.json()
            user_ids = {p["userId"] for p in data}
            if user3_id not in user_ids:
                results["DELETE /api/chat/conversations/{id}/participants/{userId}"] = True

    # 7. Підсумок
    print("\n>>> ENDPOINT SUMMARY")
    for path, ok in results.items():
        mark = "🟢" if ok else "🔴"
        print(f"{path} {mark}")

    print(">>> PARTICIPANTS TEST FINISHED")


if __name__ == "__main__":
    test_participants_flow()
