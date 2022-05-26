package app.itadakimasu.data.repository;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefRepository {
    public static final String PREFERENCE_FILE_KEY = "Shared_User_Data";
    public final static String SAVED_USERNAME_KEY = "Saved_Username";
    public final static String SAVED_PHOTO_URL_KEY = "Saved_Photo_Url";

    private static volatile SharedPrefRepository INSTANCE;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

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

    private SharedPrefRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public void setAuthUsername(String username) {
        editor.putString(SAVED_USERNAME_KEY, username);
        editor.apply();
    }

    public void setAuthUserPhotoUrl(String photoUrl) {
        editor.putString(SAVED_PHOTO_URL_KEY, photoUrl);
        editor.apply();
    }

    public String getAuthUsername() {
        return sharedPreferences.getString(SAVED_USERNAME_KEY, "");
    }

    public String getAuthUserPhotoUrl() {
        return sharedPreferences.getString(SAVED_PHOTO_URL_KEY, "");
    }
}
