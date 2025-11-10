#!/usr/bin/env bash
set -euo pipefail

echo "➡️  Creating full Spring Boot server skeleton..."

# --- Directories ---
mkdir -p server/src/main/java/app/api
mkdir -p server/src/main/java/app/dto/{auth,groups,tasks,chat}
mkdir -p server/src/main/java/app/db/{entities,repositories}
mkdir -p server/src/main/java/app/services
mkdir -p server/src/main/java/app/config
mkdir -p server/src/main/resources
mkdir -p server/src/test/java/app

# --- Config files with CONTENT ---
# pom.xml
cat > server/pom.xml <<'EOF'
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.example</groupId>
  <artifactId>csp-server</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>collab-study-platform-server</name>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.4</version>
  </parent>

  <properties>
    <java.version>17</java.version>
  </properties>

  <dependencies>
    <!-- REST -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- WebSocket -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>

    <!-- JPA + Validation -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Security (BCrypt; JWT можна додати пізніше) -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- SQLite JDBC -->
    <dependency>
      <groupId>org.xerial</groupId>
      <artifactId>sqlite-jdbc</artifactId>
      <version>3.46.0.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <!-- Tests -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
EOF

# application.yml
cat > server/src/main/resources/application.yml <<'EOF'
server:
  port: 8080

spring:
  datasource:
    url: jdbc:sqlite:./data/app.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    hibernate:
      ddl-auto: none        # використовуй schema.sql для створення таблиць
    show-sql: true
    properties:
      hibernate.format_sql: true
  sql:
    init:
      mode: always          # виконає schema.sql / data.sql при старті (якщо існують)
EOF

# SQL helpers (empty)
: > server/src/main/resources/schema.sql
: > server/src/main/resources/data.sql

# --- Java files (EMPTY stubs you will fill yourself) ---
# Root
: > server/src/main/java/app/Application.java

# api
for f in AuthController GroupController TaskController ResourceController ConversationController; do
  : > "server/src/main/java/app/api/$f.java"
done

# dto
: > server/src/main/java/app/dto/auth/RegisterRequest.java
: > server/src/main/java/app/dto/auth/LoginRequest.java
: > server/src/main/java/app/dto/groups/CreateGroupRequest.java
: > server/src/main/java/app/dto/tasks/CreateTaskRequest.java
: > server/src/main/java/app/dto/chat/CreateDirectConversationRequest.java
: > server/src/main/java/app/dto/chat/SendMessageRequest.java
: > server/src/main/java/app/dto/chat/MessageResponse.java

# db/entities
for f in User Group Membership Task Resource ActivityLog Conversation ConversationParticipant ConversationParticipantId Message; do
  : > "server/src/main/java/app/db/entities/$f.java"
done

# db/repositories
for f in UserRepository GroupRepository MembershipRepository TaskRepository ResourceRepository ActivityLogRepository ConversationRepository ConversationParticipantRepository MessageRepository; do
  : > "server/src/main/java/app/db/repositories/$f.java"
done

# services
for f in AuthService GroupService TaskService ResourceService ConversationService; do
  : > "server/src/main/java/app/services/$f.java"
done

# config
: > server/src/main/java/app/config/SecurityConfig.java
: > server/src/main/java/app/config/WebSocketConfig.java

# tests
: > server/src/test/java/app/ApplicationTests.java

echo "✅ Done. Full structure created with config files populated and all Java files empty."

# Optional summary (no 'tree' dependency required)
echo
echo "Created layout:"
find server -maxdepth 5 -type d | sed 's/[^-][^\/]*\//  /g;s/\/$//;s/^/• /'
