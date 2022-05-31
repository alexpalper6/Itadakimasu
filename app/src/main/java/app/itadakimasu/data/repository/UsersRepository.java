package app.itadakimasu.data.repository;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.model.User;

/**
 * Repository used to add data on users collection in the firetore database.
 */
public class UsersRepository {
    public static volatile UsersRepository INSTANCE;
    private FirebaseFirestore dbFirestore;
    private FirebaseAuth firebaseAuth;

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

    private UsersRepository() {
        this.dbFirestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }



    /**
     * Add a user with their data to the database.
     * When the upload is performed it could success or not, show it will return a Result in order
     * to get that result on the ViewModel and show it to the user through the UI elements.
     * @param user - the users data.
     */
    public LiveData<Result<?>> addUserToDatabase(User user) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        dbFirestore.collection(FirebaseContract.UserEntry.COLLECTION_NAME)
                .document(user.getUuid())
                .set(user).addOnCompleteListener(listener -> {
                    if (listener.isSuccessful()) {
                        result.setValue(new Result.Success<User>(user));
                    } else {
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

        dbFirestore.collection(FirebaseContract.UserEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.UserEntry.USERNAME, username)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        usernameResult.setValue(new Result.Success<Boolean>(false));
                    } else {
                        usernameResult.setValue(new Result.Success<Boolean>(true));
                    }

                } else {
                    boolean isUsernameNotFound = ((FirebaseFirestoreException)task.getException()).getCode() == FirebaseFirestoreException.Code.NOT_FOUND;
                    if (isUsernameNotFound) {
                        usernameResult.setValue(new Result.Success<Boolean>(false));
                    } else {
                        usernameResult.setValue(new Result.Error(task.getException()));
                    }
                }
            }
        });
        return usernameResult;
    }


    public LiveData<Result<?>> retrieveCurrentUserData() {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        dbFirestore.collection(FirebaseContract.UserEntry.COLLECTION_NAME)
                .document(firebaseAuth.getCurrentUser().getUid())
                .get().addOnSuccessListener(documentSnapshot -> {
                    result.setValue(new Result.Success<User>(documentSnapshot.toObject(User.class)));
                })
                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));

        return result;
    }
}
