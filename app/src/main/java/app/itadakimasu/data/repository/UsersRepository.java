package app.itadakimasu.data.repository;

import android.mtp.MtpConstants;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.net.HttpURLConnection;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.model.User;

/**
 * Repository used to add data on users collection in the firetore database.
 */
public class UsersRepository {
    private FirebaseFirestore dbFirestore;
    private FirebaseAuth firebaseAuth;

    public UsersRepository() {
        this.dbFirestore = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * TODO: Comment more
     * Add a user with their info on the database.
     * @param user
     */
    public void addUserToDatabase(User user) {
        dbFirestore.collection(FirebaseContract.UserEntry.COLLECTION_NAME)
                .document(user.getUuid())
                .set(user).addOnCompleteListener(listener -> {
                    if (listener.isSuccessful()) {
                        Log.w("User added", "User added succesfully");
                    }
        });
    }

    public void updateUsername() {

    }

    /**
     * Updates the photo url that references the user image's on the storage.
     * @param photoUrl - the url reference
     * @return a result error with the error message if it fails; if not, the result will be empty.
     */
    public LiveData<Result<Result.Error>> updateUserPhotoUrl(String photoUrl) {
        MutableLiveData<Result<Result.Error>> result = new MutableLiveData<>();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        DocumentReference userRef = dbFirestore
                .collection(FirebaseContract.UserEntry.COLLECTION_NAME)
                .document(user.getUid());

        userRef.update(FirebaseContract.UserEntry.PHOTO, photoUrl).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                result.setValue(new Result.Error(e));
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



}
