package app.itadakimasu.ui.register;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.itadakimasu.R;
import app.itadakimasu.data.AppAuthRepository;

public class RegisterViewModel extends ViewModel {
    private AppAuthRepository authRepository;
    private MutableLiveData<RegisterFormState> registerFormState;

    public RegisterViewModel() {
        this.authRepository = new AppAuthRepository();
        this.registerFormState = new MutableLiveData<>();
    }

    LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public void register(String email, String username, String password) {

    }

    public void registerDataChanged(String email, String username, String password, String repeatedPassword) {
        if (!isEmailValid(email)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_email, null, null, null));
        } else if (!isUsernameValid(username)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_username, null, null ));
        } else if (!isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, null, R.string.invalid_password, null));
        } else if (!isRepeatedPasswordValid(password, repeatedPassword)) {
            registerFormState.setValue(new RegisterFormState(null, null, null, R.string.invalid_repeated_apssword));
        } else  {
            registerFormState.setValue(new RegisterFormState(true));
        }
    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isUsernameValid(String username) {
        return username != null && !username.trim().isEmpty();
    }

    private boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile(".*[0-9].*");
        Matcher matcher = pattern.matcher(password);

        return password.length() > 8 && matcher.matches();
    }

    private boolean isRepeatedPasswordValid(String password, String repeatedPassword) {
        return repeatedPassword.equals(password);
    }

}