package app.itadakimasu.data.repository;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefRepository {
    public final static String SAVED_USERNAME_KEY = "Saved_Username";
    public final static String SAVED_PHOTO_URL_KEY = "Saved_Photo_Url";

    private static volatile SharedPrefRepository INSTANCE;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public static SharedPrefRepository getInstance(Activity activity) {
        if (INSTANCE == null) {
            synchronized (SharedPrefRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SharedPrefRepository(activity);
                }
            }
        }
        return INSTANCE;
    }

    private SharedPrefRepository(Activity activity) {
        this.sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public void setUsername(String username) {
        editor.putString(SAVED_USERNAME_KEY, username);
    }

    public void setPhotoUrl(String photoUrl) {
        editor.putString(SAVED_PHOTO_URL_KEY, photoUrl);
    }

    public String getCurrentUsername() {
        return sharedPreferences.getString(SAVED_USERNAME_KEY, "");
    }

    public String getPhotoUrl() {
        return sharedPreferences.getString(SAVED_PHOTO_URL_KEY, "");
    }
}
