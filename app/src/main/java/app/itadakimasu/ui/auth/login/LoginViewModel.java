package app.itadakimasu.ui.auth.login;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.repository.AppAuthRepository;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.UsersRepository;

/**
 * View model for the login fragment, contains the state of the login, notifying the user
 * for empty fields and the error result, used to notify the user for possible errors when logging in.
 */
public class LoginViewModel extends AndroidViewModel {
    // Repositories used to login and obtain user's data.
    private final AppAuthRepository loginRepository;
    private final UsersRepository usersRepository;
    // Repository to write authenticated user's data on the shared pref app's file.
    private final SharedPrefRepository sharedPrefRepository;
    // Form state and login error result.
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginErrorResult> loginErrorResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.loginRepository = AppAuthRepository.getInstance();
        this.usersRepository = UsersRepository.getInstance();
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
    }


    /**
     * Obtains the current user's data.
     * @return the current user's data obtained.
     */
    LiveData<Result<?>> retrieveCurrentUserData() {
        return usersRepository.retrieveCurrentUserData();
    }

    /**
     * @return the login form state as live data to observe its changes.
     */
    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    /**
     * @return the login error result as live data to observe it.
     */
    LiveData<LoginErrorResult> getLoginErrorResult() {
        return loginErrorResult;
    }

    /**
     * Logins the user to the app.
     * @param userEmail - the user's email.
     * @param password - the user's password.
     * @return the error result if the login fails.
     */
    public LiveData<Result.Error> login(String userEmail, String password) {
        return loginRepository.login(userEmail, password);
    }

    /**
     * Sets the error message that will be prompted to the user.
     * @param error - the error message when the user tries to logging in.
     */
    public void setLoginErrorResult(String error) {
        loginErrorResult.setValue(new LoginErrorResult(error));
    }

    /**
     * Updates the form state with the user's email and user's password when the users enters data.
     * @param userEmail - the user's email.
     * @param password - the users' password.
     */
    public void loginDataChanged(String userEmail, String password) {
        if (isEmailValid(userEmail) && isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(true));
        }else if (!isEmailValid(userEmail)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_email));
        } else {
            loginFormState.setValue(new LoginFormState(false));
        }
    }

    /**
     * Writes the authenticated username's name on the app's SharedPreferences file.
     * @param username - the username.
     */
    public void setAuthUsername(String username) {
        sharedPrefRepository.setAuthUsername(username);
    }

    /**
     * Sets the authenticated user's photo url on the shared apps' SharedPreferences file.
     * @param photoUrl - the photo's url.
     */
    public void setAuthUserPhotoUrl(String photoUrl) {
        sharedPrefRepository.setAuthUserPhotoUrl(photoUrl);
    }

    /**
     * Checks that the email is valid.
     * @param userEmail - the user's email.
     * @return true if the email is not null and is valid; false if else.
     */
    private boolean isEmailValid(String userEmail) {
        return userEmail != null && Patterns.EMAIL_ADDRESS.matcher(userEmail).matches();
    }

    /**
     * Checks if the password is valid.
     * @param password - the user's password.
     * @return true if the password is not null; false if is null.
     */
    private boolean isPasswordValid(String password) {
        return password != null && !password.trim().isEmpty();
    }

}