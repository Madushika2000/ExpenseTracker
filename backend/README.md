# Expense Tracker Backend

Simple REST API for the Android Expense Tracker app.

## Run

```powershell
cd backend
npm start
```

The server runs at:

```text
http://localhost:3000
```

Data is stored in:

```text
backend/data/db.json
```

## Main Endpoints

### Health check

```http
GET /health
```

### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "sajeewa",
  "password": "123456",
  "email": "sajeewa@example.com"
}
```

### Sign in

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "sajeewa",
  "password": "123456"
}
```

### Get categories

```http
GET /api/categories?userId=1
```

### Add expense

```http
POST /api/expenses
Content-Type: application/json

{
  "userId": 1,
  "amount": 1500,
  "date": "2026-05-16",
  "categoryId": 1,
  "description": "Lunch"
}
```

### Get expenses

```http
GET /api/expenses?userId=1
GET /api/expenses?userId=1&month=5&year=2026
```

### Update expense

```http
PUT /api/expenses/1
Content-Type: application/json

{
  "amount": 2000,
  "date": "2026-05-16",
  "categoryId": 1,
  "description": "Updated lunch"
}
```

### Delete expense

```http
DELETE /api/expenses/1
```

### Create or update budget

```http
POST /api/budgets
Content-Type: application/json

{
  "userId": 1,
  "amount": 10000,
  "month": 5,
  "year": 2026,
  "categoryId": 1
}
```

### Get budgets

```http
GET /api/budgets?userId=1&month=5&year=2026
```

### Save salary

```http
POST /api/salary
Content-Type: application/json

{
  "userId": 1,
  "amount": 50000,
  "month": 5,
  "year": 2026
}
```

### Dashboard summary

```http
GET /api/dashboard?userId=1&month=5&year=2026
```

Returns total expenses, remaining salary, low salary alert status, and recent expenses.

## Note

This is a starter backend for development. It stores passwords as plain text because it mirrors the current Android app. Before using it for real users, add password hashing, authentication tokens, validation, and a production database.
