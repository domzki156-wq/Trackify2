package App.dao;

import App.models.User;
import java.util.Optional;

public interface UserDao {
    User save(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findById(String id);
    void deleteAll(); // for tests/dev
}
