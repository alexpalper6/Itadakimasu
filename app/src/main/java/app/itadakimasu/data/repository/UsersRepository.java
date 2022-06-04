package app.itadakimasu.data.repository;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.model.User;

/**
 * Repository used to add data on users collection in the firetore database.
 */
public class UsersRepository {
    private static final String TAG = "UsersRepository";
    // Repository's singleton
    public static volatile UsersRepository INSTANCE;
    // Instances of data sources
    private final FirebaseFirestore dbFirestore;
    private final FirebaseAuth firebaseAuth;

    /**
     * @return the repository's singleton.
     */
    public static UsersRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (UsersRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UsersRepository();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Sets the data sources instances on the repository.
     */
    private UsersRepository() {
        this.dbFirestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }



    /**
     * Add a user with their data to the firebase firestore user collection.
     * @param user - the users data.
     * @return Result.Success with the user instance with their data; Result.Error if something goes wrong.
     */
    public LiveData<Result<?>> addUserToDatabase(User user) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "addUserToDatabase: adding user to database");
        dbFirestore.collection(FirebaseContract.UserEntry.COLLECTION_NAME)
                .document(user.getUuid())
                .set(user).addOnCompleteListener(listener -> {
                    if (listener.isSuccessful()) {
                        result.setValue(new Result.Success<User>(user));
                    } else {
                        Log.e(TAG, "addUserToDatabase: error ading user", listener.getException());
                        result.setValue(new Result.Error(listener.getException()));
                    }
        });
        return result;
    }


    /**
     * Checks if a username already exists in the database.
     * It will return a successful result with a boolean value, true if the user is found, false if it's not found;
     * and an error if it fails because something different happened.
     *
     * @param username - the username that will be searched.
     * @return a result (successful) if the user is found (true) or not (false); and error if a failure happens.
     */
    public LiveData<Result<?>> isUsernameChosen(String username) {
        MutableLiveData<Result<?>> usernameResult = new MutableLiveData<>();
        Log.i(TAG, "isUsernameChosen: obtaining user document");
        dbFirestore.collection(FirebaseContract.UserEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.UserEntry.USERNAME, username)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            usernameResult.setValue(new Result.Success<Boolean>(false));
                        } else {
                            usernameResult.setValue(new Result.Success<Boolean>(true));
                        }

                    } else {
                        Log.e(TAG, "isUsernameChosen: error obtaining document", task.getException());
                        usernameResult.setValue(new Result.Error(task.getException()));
                    }
                });
        return usernameResult;
    }

    /**
     * Obtains the authenticated user's data stored on the database.
     * @return Result.Success with their data; Result.Error if something goes wrong.
     */
    public LiveData<Result<?>> retrieveCurrentUserData() {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "retrieveCurrentUserData: obtaining authenticated user's document reference");
        dbFirestore.collection(FirebaseContract.UserEntry.COLLECTION_NAME)
                .document(firebaseAuth.getCurrentUser().getUid())
                .get().addOnSuccessListener(documentSnapshot -> result.setValue(new Result.Success<User>(documentSnapshot.toObject(User.class))))
                .addOnFailureListener(failure -> {
                    Log.d(TAG, "retrieveCurrentUserData: error obtaining the document's data", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }
}
