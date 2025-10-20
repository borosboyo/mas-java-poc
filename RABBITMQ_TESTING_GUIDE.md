# RabbitMQ Testing Guide

## Overview
This guide shows you how to test RabbitMQ message sending using the `/api/messages/send` endpoint.

## Available Notification Types
- `INFO` - Informational messages
- `WARNING` - Warning messages
- `ERROR` - Error messages
- `SUCCESS` - Success messages

---

## 🔧 cURL Examples

### 1. Send INFO Notification
```bash
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "message": "This is an informational notification",
    "recipient": "user@example.com",
    "type": "INFO"
  }'
```

### 2. Send SUCCESS Notification
```bash
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "message": "Operation completed successfully!",
    "recipient": "admin@example.com",
    "type": "SUCCESS"
  }'
```

### 3. Send WARNING Notification
```bash
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "message": "Disk space is running low",
    "recipient": "sysadmin@example.com",
    "type": "WARNING"
  }'
```

### 4. Send ERROR Notification
```bash
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "message": "Failed to process payment",
    "recipient": "support@example.com",
    "type": "ERROR"
  }'
```

---

## 📮 Postman Setup

### Step 1: Get JWT Token
First, you need to authenticate and get a JWT token.

**Request:**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "roles": ["ROLE_ADMIN"]
}
```

Copy the `token` value for the next step.

---

### Step 2: Send RabbitMQ Message

#### Postman Configuration:

**Method:** `POST`

**URL:** `http://localhost:8080/api/messages/send`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
*(Replace with your actual JWT token)*

**Body (raw JSON):**
```json
{
  "message": "New book has been added to the library",
  "recipient": "librarian@example.com",
  "type": "INFO"
}
```

**Expected Response:**
```
Message sent to RabbitMQ successfully!
```

---

## 📋 Complete Postman Examples

### Example 1: Book Added Notification
```json
{
  "message": "New book 'Clean Code' by Robert C. Martin has been added",
  "recipient": "library-team@example.com",
  "type": "SUCCESS"
}
```

### Example 2: Low Stock Warning
```json
{
  "message": "Book 'The Pragmatic Programmer' has only 2 copies remaining",
  "recipient": "inventory@example.com",
  "type": "WARNING"
}
```

### Example 3: Database Error
```json
{
  "message": "Failed to sync author data with external API",
  "recipient": "devops@example.com",
  "type": "ERROR"
}
```

### Example 4: Daily Report
```json
{
  "message": "Daily summary: 15 books borrowed, 12 returned, 3 overdue",
  "recipient": "manager@example.com",
  "type": "INFO"
}
```

---

## 🔄 Testing the Complete Flow

### 1. Make sure WebSocket test page is open
Open `http://localhost:8080/websocket-test.html` and click "Connect to WebSocket"

### 2. Send a message via Postman
Use the `/api/messages/send` endpoint as shown above

### 3. Watch the WebSocket page
You should see the notification appear in real-time on the WebSocket test page!

---

## 🐳 Docker Environment

If running in Docker, the endpoints are the same:
- **API:** `http://localhost:8080/api/messages/send`
- **WebSocket Test:** `http://localhost:8080/websocket-test.html`

---

## 🧪 Quick Test Script

Save this as `test-rabbitmq.sh`:

```bash
#!/bin/bash

# Step 1: Login and get token
echo "🔐 Logging in..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' | jq -r '.token')

echo "✅ Token received: ${TOKEN:0:20}..."

# Step 2: Send test notifications
echo ""
echo "📨 Sending INFO notification..."
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "message": "This is a test INFO message",
    "recipient": "test@example.com",
    "type": "INFO"
  }'

echo ""
echo ""
echo "📨 Sending SUCCESS notification..."
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "message": "Operation completed successfully!",
    "recipient": "test@example.com",
    "type": "SUCCESS"
  }'

echo ""
echo ""
echo "✅ Test complete! Check your WebSocket page for messages."
```

Make it executable:
```bash
chmod +x test-rabbitmq.sh
./test-rabbitmq.sh
```

---

## 📊 RabbitMQ Management UI

Access RabbitMQ management interface at:
- **URL:** `http://localhost:15672`
- **Username:** `guest`
- **Password:** `guest`

Here you can:
- View queues and exchanges
- Monitor message rates
- See connections and channels
- Debug message flow

---

## 🔍 Troubleshooting

### Issue: 401 Unauthorized
**Solution:** Make sure you included the JWT token in the Authorization header

### Issue: 403 Forbidden
**Solution:** Check that your token is valid and not expired. Get a new token by logging in again.

### Issue: Messages not appearing on WebSocket
**Solution:** 
1. Check that WebSocket is connected (websocket-test.html)
2. Verify RabbitMQ is running: `docker ps | grep rabbitmq`
3. Check application logs for errors

### Issue: Connection refused
**Solution:** Make sure Docker containers are running:
```bash
docker-compose up -d
```

---

## 💡 Tips

1. **Use Postman Environment Variables:**
   - Create variable `baseUrl` = `http://localhost:8080`
   - Create variable `token` = `{{token}}` (set after login)
   
2. **Save common requests as Postman Collection**

3. **Use Pre-request Script in Postman to auto-login:**
   ```javascript
   pm.sendRequest({
       url: 'http://localhost:8080/api/auth/login',
       method: 'POST',
       header: 'Content-Type: application/json',
       body: {
           mode: 'raw',
           raw: JSON.stringify({
               username: 'admin',
               password: 'password'
           })
       }
   }, function (err, res) {
       pm.environment.set('token', res.json().token);
   });
   ```

---

## 📝 Request Body Schema

```json
{
  "message": "string (required) - The notification message content",
  "recipient": "string (required) - Email or identifier of the recipient",
  "type": "enum (required) - One of: INFO, WARNING, ERROR, SUCCESS"
}
```

---

## ✅ Success Response
```
HTTP/1.1 200 OK
Content-Type: text/plain

Message sent to RabbitMQ successfully!
```

---

Happy Testing! 🚀

