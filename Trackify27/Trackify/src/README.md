# Trackify - Digital Business Tracker

A JavaFX desktop application for tracking business transactions, managing inventory, monitoring wallet balances, and analyzing revenue performance.

## 📋 Prerequisites

- **JDK 23** (required)
- **IntelliJ IDEA** (recommended) or any Java IDE
- **Gradle** (included via wrapper)

## 📁 Project Structure
```
Trackify/
├── build.gradle.kts          # Gradle build configuration
├── settings.gradle.kts       # Gradle settings
├── gradlew                   # Gradle wrapper (Unix)
├── gradlew.bat               # Gradle wrapper (Windows)
├── exports/                  # CSV exports folder (auto-created)
├── src/
│   ├── main/
│   │   ├── java/App/
│   │   │   ├── MainApp.java                      # Application entry point
│   │   │   ├── controllers/
│   │   │   │   ├── DashboardController.java      # Main UI controller
│   │   │   │   ├── RecordTransactionController.java
│   │   │   │   └── CurrencyConverterController.java
│   │   │   ├── models/
│   │   │   │   └── Transaction.java              # Transaction data model
│   │   │   └── dao/
│   │   │       ├── TransactionDao.java           # DAO interface
│   │   │       └── InMemoryTransactionDao.java   # In-memory implementation
│   │   └── resources/
│   │       ├── fxml/                             # UI layouts
│   │       │   ├── dashboard.fxml
│   │       │   ├── record-transaction.fxml
│   │       │   └── currency-converter.fxml
│   │       └── styles/
│   │           └── app.css                       # Dark theme stylesheet
│   └── test/
│       └── java/App/dao/
│           └── InMemoryTransactionDaoTest.java   # Unit tests
└── README.md
```

## 🚀 Quick Start

### 1. Open in IntelliJ IDEA

1. Launch IntelliJ IDEA
2. Click **File → Open**
3. Navigate to and select the **Trackify** folder
4. Click **OK**
5. IntelliJ will automatically detect the Gradle project and sync

### 2. Configure Project SDK

1. Go to **File → Project Structure** (or press `Ctrl+Alt+Shift+S`)
2. Under **Project Settings → Project**:
    - Set **SDK** to JDK 23
    - Set **Language Level** to 23
3. Click **OK**

### 3. Run the Application

#### Option A: Using Gradle Panel (Recommended)

1. Open the **Gradle** panel: **View → Tool Windows → Gradle**
2. Expand **Trackify → Tasks → application**
3. Double-click **run**

#### Option B: Command Line
```bash
# On Windows
gradlew.bat run

# On macOS/Linux
./gradlew run
```

#### Option C: Run Configuration (Alternative)

1. Right-click on `src/main/java/App/MainApp.java`
2. Select **Run 'MainApp.main()'**

**Note:** If you get "JavaFX runtime components are missing" error, use the Gradle run method instead.

## 🎯 Features

### Current Features (v1.0)
- ✅ **Transaction Management**: Record, view, and delete transactions
- ✅ **KPI Dashboard**: Real-time revenue, cost, profit tracking
- ✅ **Wallet Management**: Track USD balance with PHP conversion
- ✅ **CSV Export**: Export transaction data for external analysis
- ✅ **Currency Converter**: Convert between USD and PHP
- ✅ **Dark Theme**: Modern, professional UI design
- ✅ **Sample Data**: Pre-populated with 3 sample transactions

### Coming Soon
- 🔄 Product/Inventory Management
- 🔄 MongoDB Integration
- 🔄 Authentication & User Management
- 🔄 Advanced Analytics & Charts
- 🔄 Multi-currency Support

## 💾 Data Persistence

**Current Implementation:** In-Memory Storage
- Data is stored in RAM and **lost when application closes**
- Uses `InMemoryTransactionDao` class
- Thread-safe with `ConcurrentHashMap`

**CSV Exports:**
- Exported files are saved to `exports/` folder
- Format: `transactions-YYYYMMDD.csv`
- Survives application restarts

## 🔄 Switching to Database (MongoDB/PostgreSQL)

To add persistent database storage:

### Step 1: Add Database Dependency

Add to `build.gradle.kts`:
```kotlin
dependencies {
    // For MongoDB
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    
    // OR for PostgreSQL
    implementation("org.postgresql:postgresql:42.7.1")
}
```

### Step 2: Create Database DAO

Create `App/dao/MongoTransactionDao.java` (or `JdbcTransactionDao.java`):
```java
package App.dao;

public class MongoTransactionDao implements TransactionDao {
    private MongoCollection<Document> collection;
    
    public MongoTransactionDao(String connectionString) {
        // Initialize MongoDB connection
    }
    
    @Override
    public Transaction save(Transaction transaction) {
        // MongoDB save logic
    }
    
    // Implement other methods...
}
```

### Step 3: Update Controller

In `App/controllers/DashboardController.java`, change:
```java
// FROM:
private final TransactionDao transactionDao = new InMemoryTransactionDao();

// TO:
private final TransactionDao transactionDao = new MongoTransactionDao("mongodb://localhost:27017/trackify");
```

That's it! All controller code remains the same due to interface abstraction.

## 🧪 Running Tests

### Command Line:
```bash
# Windows
gradlew.bat test

# macOS/Linux
./gradlew test
```

### IntelliJ:
1. Right-click on `src/test/java`
2. Select **Run 'All Tests'**

Test results will appear in the console and IntelliJ's test runner.

## 🐛 Troubleshooting

### Issue: "JavaFX runtime components are missing"

**Solution:** Ensure you're using the Gradle run task, not a plain Java run configuration.
- Use `./gradlew run` from command line
- Or use the Gradle panel in IntelliJ (Trackify → Tasks → application → run)

### Issue: CSV export fails

**Solution:** The `exports/` folder is auto-created on first run.
- Check file permissions in your project directory
- Ensure you have write access to the project folder

### Issue: Gradle sync fails

**Solution:**
1. Ensure you have internet connection (Gradle needs to download dependencies)
2. Try: **File → Invalidate Caches → Invalidate and Restart**
3. Delete `.gradle` folder in project root and re-sync

## 📝 Usage Tips

1. **Recording Transactions**: Click "Record Transaction" → Fill form → Save
    - Profit is auto-calculated from revenue and cost
    - Date defaults to today

2. **Deleting Transactions**: Select a row → Click "Delete Selected"
    - Wallet balance adjusts automatically

3. **Exporting Data**: Click "Export CSV"
    - Runs on background thread
    - File appears in `exports/` folder

4. **Managing Wallet**: Use "Deposit" / "Withdraw" buttons
    - PHP equivalent updates automatically
    - Based on default exchange rate (59.07 PHP/USD)

## 🔧 Build Commands
```bash
# Clean build
./gradlew clean

# Build without running
./gradlew build

# Run application
./gradlew run

# Run tests
./gradlew test

# Create distribution
./gradlew installDist
```

## 📄 License

This project is for educational purposes as part of an OOP course.

## 👥 Authors

Trackify Team - OOP Business Proposal Project

---

**Need Help?** Check the troubleshooting section or review the inline code comments for detailed explanations.