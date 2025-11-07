package oop.barcelo.trackify27.controllers;

import org.bson.Document;

public class UserController {
    private static Document currentUser;

    public static void setCurrentUser(Document userDoc) {
        currentUser = userDoc;
    }

    public static Document getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
