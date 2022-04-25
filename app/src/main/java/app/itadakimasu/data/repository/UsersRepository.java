package app.itadakimasu.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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



}
