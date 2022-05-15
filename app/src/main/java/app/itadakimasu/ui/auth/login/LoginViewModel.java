package app.itadakimasu.ui.auth.login;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.repository.AppAuthRepository;

public class LoginViewModel extends ViewModel {

    private final AppAuthRepository loginRepository;
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginErrorResult> loginErrorResult = new MutableLiveData<>();

    public LoginViewModel() {
        this.loginRepository = AppAuthRepository.getInstance();
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginErrorResult> getLoginErrorResult() {
        return loginErrorResult;
    }

    public LiveData<Result.Error> login(String userEmail, String password) {
        return loginRepository.login(userEmail, password);
    }

    public void setLoginErrorResult(String error) {
        loginErrorResult.setValue(new LoginErrorResult(error));
    }

    public void loginDataChanged(String userEmail, String password) {
        if (isEmailValid(userEmail) && isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(true));
        }else if (!isEmailValid(userEmail)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_email));
        } else {
            loginFormState.setValue(new LoginFormState(false));
        }
    }

    private boolean isEmailValid(String userEmail) {
        return userEmail != null && Patterns.EMAIL_ADDRESS.matcher(userEmail).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && !password.trim().isEmpty();
    }



}