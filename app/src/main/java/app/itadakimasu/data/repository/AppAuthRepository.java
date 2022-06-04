package app.itadakimasu.data.repository;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.model.User;

/**
 * Repository that requests authentication and user information from the remote data source.
 */
public class AppAuthRepository {
    private final static String TAG = "AppAuthRepository";
    // Repository's singleton
    public static volatile AppAuthRepository INSTANCE;
    // Entry point of Firebase Authentication, this is used to get the instance.
    private final FirebaseAuth firebaseAuth;

    /**
     * @return app auth repository singleton
     */
    public static AppAuthRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (AppAuthRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppAuthRepository();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Sets the firebase auth data source instance.
     */
    private AppAuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Authenticates the user with given email and password, returns a result so the UI Layer can
     * interact with it.
     * @param userEmail - the email.
     * @param password - the password of the user.
     * @return a result error if the login is not successful, so the system can notify the user.
     */
    public LiveData<Result.Error> login(String userEmail, String password) {
        MutableLiveData<Result.Error> mutableLiveData = new MutableLiveData<>();
        firebaseAuth.signInWithEmailAndPassword(userEmail, password).addOnFailureListener(error -> mutableLiveData.setValue(new Result.Error(error)));
        return mutableLiveData;
    }

    /**
     * Creates an account with given email, username and password.
     * @param email - the user's email.
     * @param username - the user's name.
     * @param password - the user's password.
     * @return a result, a successful one if the registry is performed, this result will contain the
     * user's data; and an error with the message, in case the account couldn't be created.
     */
    public MutableLiveData<Result<?>> register(String email, String username, String password) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(task -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                User user = new User(firebaseUser.getUid(), username);
                user.setPhotoUrl(FirebaseContract.StoragePath.USER_PICTURES + firebaseUser.getUid());
                result.setValue(new Result.Success<User>(user));
                Log.i(TAG, "register: user created successfully");
            }

        }).addOnFailureListener(failure -> {
            Log.e(TAG, "register: error creating user", failure);
            result.setValue(new Result.Error(failure));
        });
        return result;
    }

    /**
     * Logs out the user.
     */
    public void signOut() {
        firebaseAuth.signOut();
    }
}