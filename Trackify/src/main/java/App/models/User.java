package App.models;

import java.time.Instant;
import java.util.Objects;

public class User {
    private String id;
    private String username;
    private String passwordHash; // BCrypt hash
    private Instant createdAt;

    public User() {}

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User u = (User) o;
        return Objects.equals(id, u.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
