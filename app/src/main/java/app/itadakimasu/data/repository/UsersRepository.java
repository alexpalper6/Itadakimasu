package app.itadakimasu.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.net.HttpURLConnection;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.model.User;

public class UsersRepository {
    private FirebaseFirestore dbFirestore;
    private AppAuthRepository authRepository;

    public UsersRepository() {
        this.dbFirestore = FirebaseFirestore.getInstance();
        this.authRepository = new AppAuthRepository();
    }

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

    public void updatePhoto() {

    }

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
