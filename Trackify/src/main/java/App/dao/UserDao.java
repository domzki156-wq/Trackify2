package App.dao;

import App.models.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    User save(User u);
    void updateBalance(String userId, double newBalance);
    void deleteById(String id);
}
