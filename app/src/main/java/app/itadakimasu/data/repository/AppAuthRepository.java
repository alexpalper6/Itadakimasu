package app.itadakimasu.data.repository;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.User;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class AppAuthRepository {
    private final FirebaseAuth firebaseAuth;

    public AppAuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }




    public void login(String username, String password) {

    }

    public MutableLiveData<Result<?>> register(String email, String username, String password) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUsername(user, username);
                    result.setValue(new Result.Success<FirebaseUser>(user));

                } else {
                    result.setValue(new Result.Error(task.getException()));
                }
        });
        return result;
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public boolean isSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    private void updateUsername(FirebaseUser user, String username) {
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(username)
                .build();
        user.updateProfile(profileUpdate);
    }
}