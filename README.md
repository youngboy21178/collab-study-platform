# **Collab Study Platform**

## **1\. Project Description and Application Goals**

**Collab Study Platform** is a client-server desktop application designed for students to facilitate group studying. It solves the problem of organizing study materials, communication, and task tracking in one place.  
**Key Objectives:**

* Enable the creation of study groups and member management.  
* Provide real-time communication (chat) and notifications.  
* Efficiently manage tasks (To-Do) and share study materials (files).

## **2\. System Architecture**

The application is built on a three-tier architecture:

1. **Frontend (Client \- JavaFX):**  
   * Handles user interaction (GUI).  
   * Communicates with the server via **REST API** (for data) and **WebSocket/STOMP** (for real-time chat and task updates).  
   * Stores the session token in memory (SessionStore).  
2. **Backend (Server \- Spring Boot):**  
   * Processes business logic, authentication, and authorization.  
   * Exposes REST endpoints and the WebSocket broker.  
   * Manages file uploads to local storage.  
3. **Database (SQLite):**  
   * Stores persistent data (users, groups, messages, tasks).  
   * File-based database (app.db) suitable for simple containerized deployment.

graph LR  
    A\[Client (JavaFX)\] \-- REST / HTTP \--\> B\[Server (Spring Boot)\]  
    A \-- WebSocket / STOMP \--\> B  
    B \-- JDBC \--\> C\[(Database SQLite)\]  
    B \-- I/O \--\> D\[File Storage (Uploads)\]

## **3\. Database Model (ER Diagram)**

The database is designed to support relational links between users and groups.  
*(Insert your ER diagram image above)*  
**Main Tables:**

* **users:** Stores login credentials (hashed passwords), profile info, and Google IDs.  
* **groups:** Definitions of study groups (name, owner).  
* **memberships:** M:N association table between users and groups (roles).  
* **conversations & messages:** Stores chat history (direct and group messages).  
* **tasks & task\_progress:** Task management and status tracking (OPEN, IN\_PROGRESS, DONE) for each user.  
* **resources:** Metadata about uploaded files.

## **4\. Documentation of REST API and WebSocket Endpoints**

### **REST API (Key Endpoints)**

**Authentication:**

* POST /api/auth/login \- Login (returns JWT/token).  
* POST /api/auth/register \- Register a new user.  
* GET /api/auth/oauth2/success \- Exchange Google session for an application token.

**Groups and Tasks:**

* GET /api/groups \- List user's groups.  
* POST /api/groups \- Create a new group.  
* GET /api/groups/{id}/tasks \- Get tasks for a group.  
* POST /api/tasks \- Create a new task.  
* PATCH /api/tasks/{id}/progress \- Update task status.

**Chat:**

* GET /api/chat/conversations/of-user/{id} \- List conversations.  
* GET /api/chat/{id}/messages \- Load message history.

**Materials:**

* POST /api/groups/{id}/resources \- Upload file (Multipart).  
* GET /api/resources/{id}/download \- Download file.

### **WebSocket (STOMP)**

**Endpoint:** ws://localhost:8080/ws

* **Subscribe:** /topic/conversations/{id} \- Receive new messages in chat.  
* **Subscribe:** /topic/groups/{id}/tasks \- Receive task changes (real-time update).  
* **Send:** /app/chat.sendMessage \- Send a message via socket.

## **5\. User Interface Examples**

1. **Login and Registration:** Users can log in via email or Google OAuth.  
2. **Main Dashboard:** Displays an orbital menu for navigation to Chat, Groups, and Tasks.  
3. **Chat and Groups:** Interface for real-time communication and group member management.

## **6\. Description of Challenges and Solutions**

During development, we encountered several technical issues:  
**Google OAuth and JavaFX WebView:**

* *Problem:* Google blocks logins via embedded browsers (WebView) for security reasons ("Browser not secure").  
* *Solution:* Changed User-Agent in JavaFX to simulate a modern browser (Edge/Chrome) and managed Cookies using java.net.CookieManager to transfer the session to the API client.

**Synchronizing REST and WebSocket:**

* *Problem:* How to secure WebSocket authentication when using simple tokens.  
* *Solution:* Implemented a hybrid approach – REST is used for loading history and file uploads, while WebSocket serves only for distributing new messages in real-time.

**Deployment (Docker & CI/CD):**

* *Problem:* Issues with Java image versions in Dockerfile and large file sizes during Git push.  
* *Solution:* Used multi-stage Docker build with eclipse-temurin:21 and configured .gitignore to exclude build artifacts (jpackage output).

## **7\. Evaluation of Work with AI**

AI tools (ChatGPT/Claude/Gemini) were used in the project.  
**What helped:**

* Generating boilerplate code for JavaFX controllers and FXML structure.  
* Quickly creating SQL schema for SQLite.  
* Debugging errors (e.g., 401 Unauthorized with OAuth, module-info.java dependency issues).  
* Creating configuration for Docker and GitHub Actions pipeline.

**What required manual tuning:**

* Logic for scene switching and data passing between controllers.  
* Specific AuthService logic, where AI suggested complex Spring Security chains that we had to simplify for the semester project needs.  
* CSS styling – AI generates generic designs which had to be adapted to the application theme.

**Author:** \[Your Name / Team\]