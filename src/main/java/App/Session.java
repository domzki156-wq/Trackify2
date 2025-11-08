package App;

import App.models.User;

public class Session {
    private static User currentUser;

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }
    public static boolean isAuthenticated() { return currentUser != null; }
    public static void clear() { currentUser = null; }
}
