const http = require("http");
const fs = require("fs");
const path = require("path");

const PORT = Number(process.env.PORT || 3000);
const DB_PATH = path.join(__dirname, "data", "db.json");

const defaultCategories = [
  { id: 1, name: "Food & Dining", icon: "Food", color: "#FF6B6B", userId: 0 },
  { id: 2, name: "Transport", icon: "Transport", color: "#4ECDC4", userId: 0 },
  { id: 3, name: "Education", icon: "Education", color: "#45B7D1", userId: 0 },
  { id: 4, name: "Healthcare", icon: "Healthcare", color: "#96CEB4", userId: 0 },
  { id: 5, name: "Shopping", icon: "Shopping", color: "#FFEAA7", userId: 0 },
  { id: 6, name: "Entertainment", icon: "Entertainment", color: "#DDA0DD", userId: 0 },
  { id: 7, name: "Bills & Utilities", icon: "Bills", color: "#98D8C8", userId: 0 },
  { id: 8, name: "Other", icon: "Other", color: "#B0B0B0", userId: 0 }
];

function createEmptyDb() {
  return {
    users: [],
    categories: defaultCategories,
    expenses: [],
    budgets: [],
    salaries: [],
    nextIds: {
      users: 1,
      categories: defaultCategories.length + 1,
      expenses: 1,
      budgets: 1,
      salaries: 1
    }
  };
}

function ensureDbFile() {
  fs.mkdirSync(path.dirname(DB_PATH), { recursive: true });
  if (!fs.existsSync(DB_PATH)) {
    fs.writeFileSync(DB_PATH, JSON.stringify(createEmptyDb(), null, 2));
  }
}

function readDb() {
  ensureDbFile();
  return JSON.parse(fs.readFileSync(DB_PATH, "utf8"));
}

function writeDb(db) {
  fs.writeFileSync(DB_PATH, JSON.stringify(db, null, 2));
}

function sendJson(res, statusCode, data) {
  res.writeHead(statusCode, {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET,POST,PUT,DELETE,OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type"
  });
  res.end(JSON.stringify(data));
}

function sendError(res, statusCode, message) {
  sendJson(res, statusCode, { error: message });
}

function parseBody(req) {
  return new Promise((resolve, reject) => {
    let body = "";
    req.on("data", chunk => {
      body += chunk;
      if (body.length > 1_000_000) {
        reject(new Error("Request body is too large"));
        req.destroy();
      }
    });
    req.on("end", () => {
      if (!body) {
        resolve({});
        return;
      }
      try {
        resolve(JSON.parse(body));
      } catch {
        reject(new Error("Invalid JSON body"));
      }
    });
  });
}

function numberValue(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

function requiredString(value) {
  return typeof value === "string" && value.trim().length > 0 ? value.trim() : null;
}

function normalizeMonthYear(query, body = {}) {
  const today = new Date();
  const month = numberValue(body.month ?? query.get("month")) || today.getMonth() + 1;
  const year = numberValue(body.year ?? query.get("year")) || today.getFullYear();
  return { month, year };
}

function withCategory(db, item) {
  const category = db.categories.find(c => c.id === item.categoryId);
  return {
    ...item,
    categoryName: category ? category.name : "Other",
    categoryIcon: category ? category.icon : "Other",
    categoryColor: category ? category.color : "#B0B0B0"
  };
}

function isSameMonth(date, month, year) {
  return typeof date === "string" && date.startsWith(`${year}-${String(month).padStart(2, "0")}`);
}

function getMonthlyExpenses(db, userId, month, year) {
  return db.expenses
    .filter(expense => expense.userId === userId && isSameMonth(expense.date, month, year))
    .sort((a, b) => b.date.localeCompare(a.date))
    .map(expense => withCategory(db, expense));
}

function getMonthlyExpenseTotal(db, userId, month, year) {
  return getMonthlyExpenses(db, userId, month, year)
    .reduce((total, expense) => total + expense.amount, 0);
}

function getCategoryExpenseTotal(db, userId, categoryId, month, year) {
  return db.expenses
    .filter(expense =>
      expense.userId === userId &&
      expense.categoryId === categoryId &&
      isSameMonth(expense.date, month, year)
    )
    .reduce((total, expense) => total + expense.amount, 0);
}

function getSalary(db, userId, month, year) {
  const salary = db.salaries.find(item =>
    item.userId === userId && item.month === month && item.year === year
  );
  return salary ? salary.amount : 0;
}

async function handleRequest(req, res) {
  if (req.method === "OPTIONS") {
    sendJson(res, 204, {});
    return;
  }

  const url = new URL(req.url, `http://${req.headers.host}`);
  const parts = url.pathname.split("/").filter(Boolean);
  const db = readDb();

  try {
    if (req.method === "GET" && url.pathname === "/health") {
      sendJson(res, 200, { status: "ok" });
      return;
    }

    if (req.method === "POST" && url.pathname === "/api/auth/register") {
      const body = await parseBody(req);
      const username = requiredString(body.username);
      const password = requiredString(body.password);
      if (!username || !password) {
        sendError(res, 400, "Username and password are required");
        return;
      }
      if (db.users.some(user => user.username.toLowerCase() === username.toLowerCase())) {
        sendError(res, 409, "Username already exists");
        return;
      }
      const user = {
        id: db.nextIds.users++,
        username,
        password,
        email: requiredString(body.email) || ""
      };
      db.users.push(user);
      writeDb(db);
      sendJson(res, 201, { id: user.id, username: user.username, email: user.email });
      return;
    }

    if (req.method === "POST" && url.pathname === "/api/auth/login") {
      const body = await parseBody(req);
      const username = requiredString(body.username);
      const password = requiredString(body.password);
      const user = db.users.find(item => item.username === username && item.password === password);
      if (!user) {
        sendError(res, 401, "Invalid username or password");
        return;
      }
      sendJson(res, 200, { id: user.id, username: user.username, email: user.email });
      return;
    }

    if (req.method === "GET" && url.pathname === "/api/categories") {
      const userId = numberValue(url.searchParams.get("userId"));
      if (!userId) {
        sendError(res, 400, "userId is required");
        return;
      }
      sendJson(res, 200, db.categories.filter(cat => cat.userId === 0 || cat.userId === userId));
      return;
    }

    if (req.method === "POST" && url.pathname === "/api/categories") {
      const body = await parseBody(req);
      const name = requiredString(body.name);
      const userId = numberValue(body.userId);
      if (!name || !userId) {
        sendError(res, 400, "name and userId are required");
        return;
      }
      const category = {
        id: db.nextIds.categories++,
        name,
        icon: requiredString(body.icon) || "Other",
        color: requiredString(body.color) || "#B0B0B0",
        userId
      };
      db.categories.push(category);
      writeDb(db);
      sendJson(res, 201, category);
      return;
    }

    if (req.method === "GET" && url.pathname === "/api/expenses") {
      const userId = numberValue(url.searchParams.get("userId"));
      if (!userId) {
        sendError(res, 400, "userId is required");
        return;
      }
      const month = numberValue(url.searchParams.get("month"));
      const year = numberValue(url.searchParams.get("year"));
      const expenses = month && year
        ? getMonthlyExpenses(db, userId, month, year)
        : db.expenses.filter(expense => expense.userId === userId).map(expense => withCategory(db, expense));
      sendJson(res, 200, expenses);
      return;
    }

    if (req.method === "POST" && url.pathname === "/api/expenses") {
      const body = await parseBody(req);
      const amount = numberValue(body.amount);
      const userId = numberValue(body.userId);
      const categoryId = numberValue(body.categoryId);
      const date = requiredString(body.date);
      if (!amount || amount <= 0 || !userId || !categoryId || !date) {
        sendError(res, 400, "amount, userId, categoryId, and date are required");
        return;
      }
      const expense = {
        id: db.nextIds.expenses++,
        amount,
        date,
        categoryId,
        userId,
        description: requiredString(body.description) || ""
      };
      db.expenses.push(expense);
      writeDb(db);
      sendJson(res, 201, withCategory(db, expense));
      return;
    }

    if (parts[0] === "api" && parts[1] === "expenses" && parts[2]) {
      const expenseId = numberValue(parts[2]);
      const index = db.expenses.findIndex(expense => expense.id === expenseId);
      if (index === -1) {
        sendError(res, 404, "Expense not found");
        return;
      }

      if (req.method === "PUT") {
        const body = await parseBody(req);
        const amount = numberValue(body.amount);
        const categoryId = numberValue(body.categoryId);
        const date = requiredString(body.date);
        if (!amount || amount <= 0 || !categoryId || !date) {
          sendError(res, 400, "amount, categoryId, and date are required");
          return;
        }
        db.expenses[index] = {
          ...db.expenses[index],
          amount,
          date,
          categoryId,
          description: requiredString(body.description) || ""
        };
        writeDb(db);
        sendJson(res, 200, withCategory(db, db.expenses[index]));
        return;
      }

      if (req.method === "DELETE") {
        const deleted = db.expenses.splice(index, 1)[0];
        writeDb(db);
        sendJson(res, 200, { deleted: true, id: deleted.id });
        return;
      }
    }

    if (req.method === "GET" && url.pathname === "/api/budgets") {
      const userId = numberValue(url.searchParams.get("userId"));
      if (!userId) {
        sendError(res, 400, "userId is required");
        return;
      }
      const { month, year } = normalizeMonthYear(url.searchParams);
      const budgets = db.budgets
        .filter(item => item.userId === userId && item.month === month && item.year === year)
        .map(item => {
          const spent = getCategoryExpenseTotal(db, userId, item.categoryId, month, year);
          return {
            ...withCategory(db, item),
            spent,
            percentage: item.amount > 0 ? (spent / item.amount) * 100 : 0
          };
        });
      sendJson(res, 200, budgets);
      return;
    }

    if (req.method === "POST" && url.pathname === "/api/budgets") {
      const body = await parseBody(req);
      const amount = numberValue(body.amount);
      const userId = numberValue(body.userId);
      const categoryId = numberValue(body.categoryId);
      const { month, year } = normalizeMonthYear(url.searchParams, body);
      if (!amount || amount <= 0 || !userId || !categoryId) {
        sendError(res, 400, "amount, userId, and categoryId are required");
        return;
      }
      let budget = db.budgets.find(item =>
        item.userId === userId &&
        item.categoryId === categoryId &&
        item.month === month &&
        item.year === year
      );
      if (budget) {
        budget.amount = amount;
      } else {
        budget = { id: db.nextIds.budgets++, amount, month, year, categoryId, userId };
        db.budgets.push(budget);
      }
      writeDb(db);
      sendJson(res, 200, budget);
      return;
    }

    if (req.method === "GET" && url.pathname === "/api/salary") {
      const userId = numberValue(url.searchParams.get("userId"));
      if (!userId) {
        sendError(res, 400, "userId is required");
        return;
      }
      const { month, year } = normalizeMonthYear(url.searchParams);
      sendJson(res, 200, { userId, month, year, amount: getSalary(db, userId, month, year) });
      return;
    }

    if (req.method === "POST" && url.pathname === "/api/salary") {
      const body = await parseBody(req);
      const amount = numberValue(body.amount);
      const userId = numberValue(body.userId);
      const { month, year } = normalizeMonthYear(url.searchParams, body);
      if (!amount || amount <= 0 || !userId) {
        sendError(res, 400, "amount and userId are required");
        return;
      }
      let salary = db.salaries.find(item =>
        item.userId === userId && item.month === month && item.year === year
      );
      if (salary) {
        salary.amount = amount;
      } else {
        salary = { id: db.nextIds.salaries++, userId, month, year, amount };
        db.salaries.push(salary);
      }
      writeDb(db);
      sendJson(res, 200, salary);
      return;
    }

    if (req.method === "GET" && url.pathname === "/api/dashboard") {
      const userId = numberValue(url.searchParams.get("userId"));
      if (!userId) {
        sendError(res, 400, "userId is required");
        return;
      }
      const { month, year } = normalizeMonthYear(url.searchParams);
      const expenses = getMonthlyExpenses(db, userId, month, year);
      const totalExpense = expenses.reduce((total, expense) => total + expense.amount, 0);
      const salary = getSalary(db, userId, month, year);
      sendJson(res, 200, {
        userId,
        month,
        year,
        salary,
        totalExpense,
        remainingSalary: salary - totalExpense,
        lowSalaryAlert: salary > 0 && salary - totalExpense < 5000,
        transactionCount: expenses.length,
        recentExpenses: expenses.slice(0, 5)
      });
      return;
    }

    sendError(res, 404, "Route not found");
  } catch (error) {
    sendError(res, 500, error.message || "Server error");
  }
}

const server = http.createServer(handleRequest);

server.listen(PORT, () => {
  ensureDbFile();
  console.log(`Expense Tracker backend running at http://localhost:${PORT}`);
});
