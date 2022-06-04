package app.itadakimasu.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Repository used to obtain the authenticated user data stored on a app's shared preferences file.
 * This will only contain the data of the logged user in order to obtain this data from the client side
 * instead of fetching the data from the database.
 */
public class SharedPrefRepository {
    // Constants with the file's name and its fields.
    public static final String PREFERENCE_FILE_KEY = "Shared_User_Data";
    public final static String SAVED_USERNAME_KEY = "Saved_Username";
    public final static String SAVED_PHOTO_URL_KEY = "Saved_Photo_Url";

    // Repository's singleton
    private static volatile SharedPrefRepository INSTANCE;
    // Shared preferences class used to retrieve the data from the file.
    private final SharedPreferences sharedPreferences;
    // Shared preferences editor used to being able to write data on the file.
    private final SharedPreferences.Editor editor;

    /**
     * @param context - app's context required to instantiate the Shared Preferences object.
     * @return the repository's singleton.
     */
    public static SharedPrefRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SharedPrefRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SharedPrefRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Instantiates the shared preferences object pointing to the file that stores the user's data.
     * @param context - the app's context.
     */
    private SharedPrefRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    /**
     * Writes the username on the user file's filed.
     * @param username -  the username.
     */
    public void setAuthUsername(String username) {
        editor.putString(SAVED_USERNAME_KEY, username);
        editor.apply();
    }

    /**
     * Writes the user's photo url path, where their image is stored on firebase storage, on the photo file's field.
     * @param photoUrl - the user's photo url path.
     */
    public void setAuthUserPhotoUrl(String photoUrl) {
        editor.putString(SAVED_PHOTO_URL_KEY, photoUrl);
        editor.apply();
    }

    /**
     * @return the authenticated user's username stored on the file.
     */
    public String getAuthUsername() {
        return sharedPreferences.getString(SAVED_USERNAME_KEY, "");
    }

    /**
     * @return the authenticated user's photo url path stored on the file.
     */
    public String getAuthUserPhotoUrl() {
        return sharedPreferences.getString(SAVED_PHOTO_URL_KEY, "");
    }
}
