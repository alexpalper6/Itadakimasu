package app.itadakimasu.data.model;

/**
 * Constant for every collection and field values on firebase database.
 */
public class FirebaseContract {
    public static class UserEntry {
        public static final String COLLECTION_NAME = "users";
        public static final String USERNAME = "username";
        public static final String PHOTO = "photoUrl";
    }

    public static class RecipeEntry {
        public static final String COLLECTION_NAME = "recipes";
        public static final String AUTHOR = "author";
        public static final String AUTHOR_PHOTO = "photoAuthorUrl";
        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String PHOTO = "photoUrl";
        public static final String CREATION_DATE = "creationDate";

        public static class IngredientEntry {
            public static final String COLLECTION_NAME = "ingredients";
            public static final String POSITION = "ingredientPosition";
            public static final String DESCRIPTION = "ingredientDescription";
        }

        public static class StepEntry {
            public static final String COLLECTION_NAME = "steps";
            public static final String POSITION = "stepPosition";
            public static final String DESCRIPTION = "stepDescription";
        }


    }

    public static class FavouritesEntry {
        public static final String COLLECTION_NAME = "favourites";
        public static final String ID = "id";
        public static final String USERNAME = "username";
        public static final String RECIPE_ID = "recipeId";
        public static final String ADDITION_DATE = "additionDate";
    }

    public static class StoragePath {
        public static final String USER_PICTURES = "/users/";
        public static final String RECIPES_PICTURES = "/recipes/";
    }
}
