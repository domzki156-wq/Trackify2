package App;

import App.models.User;

/**
 * Simple singleton holding currently authenticated user for the desktop app.
 */
public class Session {
    private static User current;

    public static void setCurrentUser(User user) { current = user; }
    public static User getCurrentUser() { return current; }
    public static boolean isAuthenticated() { return current != null; }
    public static void clear() { current = null; }
}
