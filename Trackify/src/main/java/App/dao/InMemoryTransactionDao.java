package App.dao;

import App.models.Transaction;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory DAO for testing (keeps the same Transaction shape with userId).
 */
public class InMemoryTransactionDao implements TransactionDao {
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private static final DateTimeFormatter CSV_DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction == null) throw new IllegalArgumentException("Transaction cannot be null");
        if (transaction.getId() == null || transaction.getId().isBlank()) {
            transaction.setId(UUID.randomUUID().toString());
        }
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(transactions.get(id));
    }

    @Override
    public List<Transaction> findAll() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(Transaction::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAllForUser(String userId) {
        if (userId == null) return Collections.emptyList();
        return transactions.values().stream()
                .filter(t -> userId.equals(t.getUserId()))
                .sorted(Comparator.comparing(Transaction::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        return transactions.remove(id) != null;
    }

    @Override
    public void exportCsv(Path outputPath) throws Exception {
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(outputPath)) {
            writer.write("Date,Customer,Item,Payment Method,Revenue,Cost,Profit,Notes");
            writer.newLine();
            for (Transaction t : findAll()) {
                String line = String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%s",
                        t.getDate() == null ? "" : t.getDate().toString(),
                        escapeCsv(t.getCustomer()),
                        escapeCsv(t.getItem()),
                        escapeCsv(t.getPaymentMethod()),
                        t.getRevenue(),
                        t.getCost(),
                        t.getProfit(),
                        escapeCsv(t.getNotes()));
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private String escapeCsv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    @Override
    public void deleteAll() { transactions.clear(); }

    @Override
    public int count() { return transactions.size(); }
}
