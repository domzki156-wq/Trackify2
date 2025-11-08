package App.dao;

import App.models.Transaction;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Transaction DAO interface.
 */
public interface TransactionDao {
    Transaction save(Transaction t);
    Optional<Transaction> findById(String id);
    List<Transaction> findAll();
    List<Transaction> findAllForUser(String userId);
    boolean deleteById(String id);
    void exportCsv(Path outputPath) throws Exception;
    void deleteAll();
    int count();
}
