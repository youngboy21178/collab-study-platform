import uuid
import requests

BASE_URL = "http://localhost:8080/api"


def random_email():
    return f"test_{uuid.uuid4().hex[:8]}@example.com"


def print_response(label, r: requests.Response):
    print(f"\n=== {label} ===")
    print("STATUS:", r.status_code)
    print("RAW BODY:", repr(r.text))
    print("HEADERS:", r.headers)


def parse_user_id_from_login(text: str) -> int | None:
    # expects something like: "login ok, userId=4"
    if "userId=" not in text:
        return None
    try:
        part = text.split("userId=")[1]
        return int(part.strip())
    except Exception:
        return None


def test_full_flow_register_login_group_task_progress():
    print(">>> START FULL FLOW TEST")

    # 0) health (може бути 404 – це ок)
    try:
        pong = requests.get("http://localhost:8080/actuator/health")
        print_response("HEALTHCHECK /actuator/health", pong)
    except Exception as e:
        print("Could not call /actuator/health (it's ok). Error:", e)

    # 1) Register user (очікуємо текст)
    email = random_email()
    register_payload = {
        "name": "Maksym Test",
        "email": email,
        "password": "secret123"
    }
    r = requests.post(f"{BASE_URL}/auth/register", json=register_payload)
    print_response("POST /auth/register", r)

    if r.status_code != 200:
        raise RuntimeError(f"Register failed: status={r.status_code}, body={r.text}")

    # 2) Login → парсимо userId з тексту
    login_payload = {
        "email": email,
        "password": "secret123"
    }
    r = requests.post(f"{BASE_URL}/auth/login", json=login_payload)
    print_response("POST /auth/login", r)

    if r.status_code != 200:
        raise RuntimeError(f"Login failed: status={r.status_code}, body={r.text}")

    user_id = parse_user_id_from_login(r.text)
    if user_id is None:
        raise RuntimeError(f"Could not parse userId from login response: {r.text}")

    print("Parsed user_id from login:", user_id)

    # 3) Create group — додаємо creatorUserId як query param
    group_payload = {
        "ownerUserId": user_id,
        "name": "PythonTestGroup",
        "description": "Group created from api_tests.py",
        "avatarUrl": None
    }

    r = requests.post(
        f"{BASE_URL}/groups",
        json=group_payload,
    )
    print_response("POST /groups", r)

    if not r.ok:
        raise RuntimeError(f"Create group failed: status={r.status_code}, body={r.text}")

    group = r.json()
    group_id = group.get("groupId")
    print(f"Parsed group_id from response: {group_id}")
    # 4) Create task in group
    task_payload = {
        "groupId": group_id,
        "creatorUserId": user_id,
        "title": "Watch lecture 1",
        "description": "Created from api_tests.py",
        "dueDate": "2025-12-10"
    }
    r = requests.post(f"{BASE_URL}/tasks", json=task_payload)
    print_response("POST /tasks", r)

    if not r.ok:
        raise RuntimeError(f"Create task failed: status={r.status_code}, body={r.text}")

    if not r.headers.get("Content-Type", "").startswith("application/json"):
        raise RuntimeError(
            "Create task did not return JSON. "
            f"Status={r.status_code}, body={r.text}"
        )

    task = r.json()
    print("Task JSON:", task)
    if "taskId" not in task:
        raise RuntimeError(f"Task JSON has no 'taskId': {task}")
    task_id = task["taskId"]
    print("Created task_id:", task_id)

    # 5) Assign user to task
    assign_payload = {
        "userId": user_id
    }
    r = requests.post(f"{BASE_URL}/tasks/{task_id}/assign", json=assign_payload)
    print_response(f"POST /tasks/{task_id}/assign", r)

    if not r.ok:
        raise RuntimeError(f"Assign task failed: status={r.status_code}, body={r.text}")

    if not r.headers.get("Content-Type", "").startswith("application/json"):
        raise RuntimeError(
            "Assign task did not return JSON. "
            f"Status={r.status_code}, body={r.text}"
        )

    progress = r.json()
    print("Assign JSON:", progress)

    # 6) Update user's task status
    update_status_payload = {
        "userId": user_id,
        "status": "IN_PROGRESS"
    }
    r = requests.patch(f"{BASE_URL}/tasks/{task_id}/progress", json=update_status_payload)
    print_response(f"PATCH /tasks/{task_id}/progress", r)

    if not r.ok:
        raise RuntimeError(f"Update progress failed: status={r.status_code}, body={r.text}")

    if not r.headers.get("Content-Type", "").startswith("application/json"):
        raise RuntimeError(
            "Update progress did not return JSON. "
            f"Status={r.status_code}, body={r.text}"
        )

    progress2 = r.json()
    print("Update progress JSON:", progress2)

    # 7) Get all tasks for group
    r = requests.get(f"{BASE_URL}/groups/{group_id}/tasks")
    print_response(f"GET /groups/{group_id}/tasks", r)

    if not r.ok:
        raise RuntimeError(f"Get group tasks failed: status={r.status_code}, body={r.text}")

    if not r.headers.get("Content-Type", "").startswith("application/json"):
        raise RuntimeError(
            "Get group tasks did not return JSON. "
            f"Status={r.status_code}, body={r.text}"
        )

    tasks_list = r.json()
    print("Tasks list JSON:", tasks_list)

    # 8) Get progress list for task
    r = requests.get(f"{BASE_URL}/tasks/{task_id}/progress")
    print_response(f"GET /tasks/{task_id}/progress", r)

    if not r.ok:
        raise RuntimeError(f"Get task progress failed: status={r.status_code}, body={r.text}")

    if not r.headers.get("Content-Type", "").startswith("application/json"):
        raise RuntimeError(
            "Get task progress did not return JSON. "
            f"Status={r.status_code}, body={r.text}"
        )

    progress_list = r.json()
    print("Progress list JSON:", progress_list)

    print("\n>>> FULL FLOW TEST FINISHED ✅")


if __name__ == "__main__":
    test_full_flow_register_login_group_task_progress()
