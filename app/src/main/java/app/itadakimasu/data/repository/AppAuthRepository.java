package app.itadakimasu.data.repository;


import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;
import java.util.concurrent.Executor;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.User;

/**
 * Class that requests authentication and user information from the remote data source.
 */
public class AppAuthRepository {
    // Entry point of Firebase Authentication, this is used to get the instance.
    private final FirebaseAuth firebaseAuth;

    public AppAuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();

    }

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


    public MutableLiveData<Result<?>> register(String email, String username, String password) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    updateDisplayName(username);
                    User user = new User(firebaseUser.getUid(), username);
                    result.setValue(new Result.Success<User>(user));
                }

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

    public void updateDisplayName(String username) {
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest
                .Builder()
                .setDisplayName(username)
                .build();
        Objects.requireNonNull(firebaseAuth.getCurrentUser()).updateProfile(profileUpdate);
    }
}