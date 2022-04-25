package app.itadakimasu.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

import app.itadakimasu.R;
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


    public boolean isLoggedIn() {
        return false;
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    private void setLoggedInUser() {

    }

    public void login(String username, String password) {

    }

    public MutableLiveData<Result<?>> register(String email, String username, String password) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        updateUsername(user, username);
                        result.setValue(new Result.Success<FirebaseUser>(user));
                    }
                } else {
                    result.setValue(new Result.Error(task.getException()));
                }
        });
        return result;
    }

    private void updateUsername(FirebaseUser user, String username) {
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(username)
                .build();
        user.updateProfile(profileUpdate);
    }
}