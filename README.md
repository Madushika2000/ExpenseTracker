# Personal Expense Tracker - Android App
## Final Year Project | I.S.M. Dissanayaka | KAN/IT/2324/P/028

---

## Project Structure

```
ExpenseTracker/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/expensetracker/
│   │   ├── activities/
│   │   │   ├── LoginActivity.java        ← Login screen
│   │   │   ├── RegisterActivity.java     ← Register screen
│   │   │   ├── MainActivity.java         ← Home with bottom nav
│   │   │   └── AddExpenseActivity.java   ← Add/Edit expense
│   │   ├── fragments/
│   │   │   ├── DashboardFragment.java    ← Overview + recent expenses
│   │   │   ├── ExpensesFragment.java     ← All expenses list
│   │   │   ├── BudgetFragment.java       ← Monthly budget management
│   │   │   └── ReportsFragment.java      ← Pie + Bar charts
│   │   ├── database/
│   │   │   └── DatabaseHelper.java       ← SQLite CRUD operations
│   │   ├── models/
│   │   │   ├── User.java
│   │   │   ├── Expense.java
│   │   │   ├── Category.java
│   │   │   └── Budget.java
│   │   ├── adapters/
│   │   │   ├── ExpenseAdapter.java       ← RecyclerView for expenses
│   │   │   └── BudgetAdapter.java        ← RecyclerView for budgets
│   │   └── utils/
│   │       └── SessionManager.java       ← SharedPreferences login
│   └── res/
│       ├── layout/                       ← All XML layouts
│       ├── drawable/                     ← Shapes & backgrounds
│       ├── menu/                         ← Bottom nav menu
│       ├── color/                        ← Color selectors
│       └── values/                       ← colors, strings, styles
├── build.gradle                          ← App dependencies
└── settings.gradle                       ← JitPack repo (for charts)
```

---

## How to Set Up in Android Studio

### Step 1: Open Project
1. Open **Android Studio**
2. Click **File → Open**
3. Select the `ExpenseTracker` folder
4. Wait for Gradle sync to finish

### Step 2: Sync Dependencies
The app uses these libraries (auto-downloaded):
- `Material Components` (UI)
- `RecyclerView`, `CardView` (lists)
- `MPAndroidChart` (pie & bar charts) — from JitPack

If sync fails:
- Go to **File → Invalidate Caches → Restart**
- Then **File → Sync Project with Gradle Files**

### Step 3: Run the App
- Connect an Android phone (USB debugging ON), OR
- Use the built-in **Android Emulator** (API 24+)
- Click the **▶ Run** button

---

## App Features

| Feature | Details |
|---------|---------|
| 🔐 Login / Register | Local authentication, session saved |
| ➕ Add Expense | Amount, category, date, description |
| ✏️ Edit Expense | Long-press any expense |
| 🗑️ Delete Expense | Long-press → Delete with confirmation |
| 📊 Budget Planning | Set monthly limits per category |
| ⚠️ Budget Alerts | Warning at 80%, danger at 100% |
| 📈 Reports | Pie chart (by category) + Bar chart (6 months) |
| 🗄️ Offline-First | All data stored locally in SQLite |

### Default Categories
Food & Dining, Transport, Education, Healthcare, Shopping, Entertainment, Bills & Utilities, Other

---

## Database Schema

```sql
users       (id, username, password, email)
categories  (id, name, icon, color, user_id)
expenses    (id, amount, date, category_id, user_id, description)
budgets     (id, amount, month, year, category_id, user_id)
```

---

## Technologies Used
- **Language:** Java
- **Database:** SQLite (via SQLiteOpenHelper)
- **UI:** Material Design Components
- **Charts:** MPAndroidChart v3.1.0
- **Min SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 14 (API 34)

---

## Troubleshooting

**"Cannot resolve symbol" errors?**
→ Run: Build → Clean Project → Rebuild Project

**Gradle sync fails?**
→ Check internet connection (first sync downloads libraries)
→ Make sure `settings.gradle` has `maven { url 'https://jitpack.io' }`

**Charts not showing?**
→ Add data first (charts are empty with no expenses)

---

*ATI Kandy — Department of Information Technology — 2026*
