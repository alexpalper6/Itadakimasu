package app.itadakimasu.data.repository;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.model.User;

/**
 * Class that requests authentication and user information from the remote data source.
 */
public class AppAuthRepository {
    public static volatile AppAuthRepository INSTANCE;
    // Entry point of Firebase Authentication, this is used to get the instance.
    private final FirebaseAuth firebaseAuth;

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
        firebaseAuth.signInWithEmailAndPassword(userEmail, password).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mutableLiveData.setValue(new Result.Error(e));
            }
        });
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

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    User user = new User(firebaseUser.getUid(), username);
                    user.setPhotoUrl(FirebaseContract.StoragePath.USER_PICTURES + firebaseUser.getUid());
                    result.setValue(new Result.Success<User>(user));
                }

            } else {
                result.setValue(new Result.Error(task.getException()));
            }
        });
        return result;
    }

    /**
     * Logs out the user.
     */
    public void signOut() {
        firebaseAuth.signOut();
    }

    /**
     * Obtains the current user.
     * @return a FirebaseUser instance with the user's authentication data.
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

}