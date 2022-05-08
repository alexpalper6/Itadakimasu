package app.itadakimasu.data.model;

public class FirebaseContract {
    public static class UserEntry {
        public static final String COLLECTION_NAME = "users";
        public static final String USERNAME = "username";
        public static final String PHOTO = "photoUrl";
    }
    public static class StorageReference {
        public static final String USER_PICTURES = "/users/";
        public static final String RECIPES_PICTURES = "/recipes/";
    }
}
