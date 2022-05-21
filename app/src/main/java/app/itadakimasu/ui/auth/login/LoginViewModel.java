package app.itadakimasu.ui.auth.login;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.User;
import app.itadakimasu.data.repository.AppAuthRepository;
import app.itadakimasu.data.repository.UsersRepository;

/**
 * View model for the login framgnet, contains the state of the login, notifying the user
 * for empty fields and the error result, used to notify the user for possible errors when logging in.
 */
public class LoginViewModel extends ViewModel {
    // Repositories used to login and obtain user's data.
    private final AppAuthRepository loginRepository;
    private final UsersRepository usersRepository;
    // Form state and login error result.
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginErrorResult> loginErrorResult = new MutableLiveData<>();

    public LoginViewModel() {
        this.loginRepository = AppAuthRepository.getInstance();
        this.usersRepository = UsersRepository.getInstance();
    }


    /**
     * Obtains the current user's data.
     * @return the current user's data obtained.
     */
    LiveData<User> retrieveCurrentUserData() {
        return usersRepository.retrieveCurrentUserData();
    }
    //

    /**
     * @return the login form state as live data to observe it.
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
     * @return the error result that is not null if the login fails.
     */
    public LiveData<Result.Error> login(String userEmail, String password) {
        return loginRepository.login(userEmail, password);
    }

    /**
     * Sets the error message that will be prompted to the user.
     * @param error - the error message that will be prompted to the user.
     */
    public void setLoginErrorResult(String error) {
        loginErrorResult.setValue(new LoginErrorResult(error));
    }

    /**
     * Updates the form state with the user's email and user's password.
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