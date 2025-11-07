package App.dao;

import App.models.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryTransactionDao.
 * Tests CRUD operations and CSV export functionality.
 *
 * @author Trackify Team
 * @version 1.0
 */
class InMemoryTransactionDaoTest {

    private TransactionDao dao;

    @BeforeEach
    void setUp() {
        dao = new InMemoryTransactionDao();
    }

    @Test
    void testSaveAndFindById() {
        // Create a transaction
        Transaction transaction = new Transaction(
                LocalDate.now(),
                "John Doe",
                "Test Item",
                "Cash",
                100.0,
                50.0,
                "Test notes"
        );

        // Save it
        Transaction saved = dao.save(transaction);
        assertNotNull(saved);
        assertNotNull(saved.getId());

        // Find it by ID
        Optional<Transaction> found = dao.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getCustomer());
        assertEquals(100.0, found.get().getRevenue());
        assertEquals(50.0, found.get().getCost());
        assertEquals(50.0, found.get().getProfit());
    }

    @Test
    void testFindAll() {
        // Initially empty
        assertEquals(0, dao.count());

        // Add multiple transactions
        dao.save(new Transaction(LocalDate.now(), "Customer1", "Item1", "Cash", 100.0, 50.0, ""));
        dao.save(new Transaction(LocalDate.now(), "Customer2", "Item2", "Card", 200.0, 100.0, ""));
        dao.save(new Transaction(LocalDate.now(), "Customer3", "Item3", "PayPal", 300.0, 150.0, ""));

        // Verify count and retrieval
        assertEquals(3, dao.count());
        List<Transaction> all = dao.findAll();
        assertEquals(3, all.size());
    }

    @Test
    void testDeleteById() {
        // Create and save a transaction
        Transaction transaction = new Transaction(
                LocalDate.now(),
                "Jane Smith",
                "Delete Test",
                "GCash",
                75.0,
                25.0,
                ""
        );
        Transaction saved = dao.save(transaction);

        // Verify it exists
        assertTrue(dao.findById(saved.getId()).isPresent());
        assertEquals(1, dao.count());

        // Delete it
        boolean deleted = dao.deleteById(saved.getId());
        assertTrue(deleted);

        // Verify it's gone
        assertFalse(dao.findById(saved.getId()).isPresent());
        assertEquals(0, dao.count());

        // Try to delete again (should return false)
        boolean deletedAgain = dao.deleteById(saved.getId());
        assertFalse(deletedAgain);
    }

    @Test
    void testDeleteAll() {
        // Add multiple transactions
        dao.save(new Transaction(LocalDate.now(), "Customer1", "Item1", "Cash", 100.0, 50.0, ""));
        dao.save(new Transaction(LocalDate.now(), "Customer2", "Item2", "Card", 200.0, 100.0, ""));
        assertEquals(2, dao.count());

        // Delete all
        dao.deleteAll();
        assertEquals(0, dao.count());
        assertTrue(dao.findAll().isEmpty());
    }

    @Test
    void testExportCsv(@TempDir Path tempDir) throws Exception {
        // Add test data
        dao.save(new Transaction(
                LocalDate.of(2025, 9, 13),
                "Test Customer",
                "Test Item",
                "PayPal",
                100.50,
                50.25,
                "Test note"
        ));

        // Export to CSV
        Path csvFile = tempDir.resolve("test-export.csv");
        dao.exportCsv(csvFile);

        // Verify file was created
        assertTrue(Files.exists(csvFile));

        // Read and verify content
        List<String> lines = Files.readAllLines(csvFile);
        assertTrue(lines.size() >= 2); // Header + at least 1 data row
        assertEquals("Date,Customer,Item,Payment Method,Revenue,Cost,Profit,Notes", lines.get(0));
        assertTrue(lines.get(1).contains("Test Customer"));
        assertTrue(lines.get(1).contains("Test Item"));
        assertTrue(lines.get(1).contains("100.50"));
    }

    @Test
    void testSaveNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> dao.save(null));
    }

    @Test
    void testProfitCalculation() {
        Transaction transaction = new Transaction(
                LocalDate.now(),
                "Profit Test",
                "Item",
                "Cash",
                200.0,
                75.0,
                ""
        );

        Transaction saved = dao.save(transaction);
        assertEquals(125.0, saved.getProfit(), 0.01);
    }

    @Test
    void testUpdateExistingTransaction() {
        // Create and save
        Transaction original = new Transaction(
                LocalDate.now(),
                "Original Customer",
                "Original Item",
                "Cash",
                100.0,
                50.0,
                ""
        );
        Transaction saved = dao.save(original);
        String id = saved.getId();

        // Modify and save again (update)
        saved.setCustomer("Updated Customer");
        saved.setRevenue(200.0);
        dao.save(saved);

        // Verify update
        Optional<Transaction> updated = dao.findById(id);
        assertTrue(updated.isPresent());
        assertEquals("Updated Customer", updated.get().getCustomer());
        assertEquals(200.0, updated.get().getRevenue());
        assertEquals(1, dao.count()); // Should still be only 1 transaction
    }

    @Test
    void testTransactionsSortedByDateDescending() {
        // Add transactions with different dates
        dao.save(new Transaction(LocalDate.of(2025, 1, 1), "C1", "I1", "Cash", 100.0, 50.0, ""));
        dao.save(new Transaction(LocalDate.of(2025, 12, 31), "C2", "I2", "Cash", 100.0, 50.0, ""));
        dao.save(new Transaction(LocalDate.of(2025, 6, 15), "C3", "I3", "Cash", 100.0, 50.0, ""));

        List<Transaction> all = dao.findAll();

        // Should be sorted newest first
        assertEquals(LocalDate.of(2025, 12, 31), all.get(0).getDate());
        assertEquals(LocalDate.of(2025, 6, 15), all.get(1).getDate());
        assertEquals(LocalDate.of(2025, 1, 1), all.get(2).getDate());
    }
}