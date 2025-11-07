package App.service;

import App.dao.UserDao;
import App.models.User;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.util.Optional;

public class AuthService {
    private final UserDao userDao;
    private final int cost = 10; // BCrypt work factor

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Register new user. Returns created User on success.
     * Throws IllegalArgumentException for duplicate username.
     */
    public User register(String username, char[] password) {
        // basic validation
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if (password == null || password.length == 0) throw new IllegalArgumentException("Password required");

        Optional<User> existing = userDao.findByUsername(username);
        if (existing.isPresent()) throw new IllegalArgumentException("Username already exists");

        String hash = BCrypt.withDefaults().hashToString(cost, password);
        // wipe password char[]? Could clear after use.
        User u = new User(username, hash);
        userDao.save(u);
        return u;
    }

    /**
     * Verify credentials. Returns User if verified, otherwise empty.
     */
    public Optional<User> login(String username, char[] password) {
        Optional<User> maybe = userDao.findByUsername(username);
        if (maybe.isEmpty()) return Optional.empty();
        User user = maybe.get();
        BCrypt.Result result = BCrypt.verifyer().verify(password, user.getPasswordHash());
        if (result.verified) return Optional.of(user);
        return Optional.empty();
    }
}
