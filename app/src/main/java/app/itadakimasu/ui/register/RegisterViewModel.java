package app.itadakimasu.ui.register;

import android.app.Application;
import android.util.Patterns;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.User;
import app.itadakimasu.data.repository.AppAuthRepository;
import app.itadakimasu.data.repository.UsersRepository;

public class RegisterViewModel extends ViewModel {
    private AppAuthRepository authRepository;
    private UsersRepository usersRepository;
    private MutableLiveData<RegisterFormState> registerFormState;
    private MutableLiveData<RegisterResult> registerResult;

    public RegisterViewModel() {
        this.authRepository = new AppAuthRepository();
        this.usersRepository = new UsersRepository();
        this.registerFormState = new MutableLiveData<>();
        this.registerResult = new MutableLiveData<>();
    }

    LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    LiveData<RegisterResult> getRegisterResult() { return registerResult; }

    public void registerResultChanged(Result<?> result) {
        if (result instanceof Result.Success) {
            User user = ((Result.Success<User>) result).getData();
            usersRepository.addUserToDatabase(user);
            registerResult.setValue(new RegisterResult(user));
        } else {
            registerResult.setValue(new RegisterResult(((Result.Error) result).getError().getMessage()));
        }
    }

    public LiveData<Result<?>> register(String email, String username, String password) {
        return authRepository.register(email, username, password);
    }

    public void registerDataChanged(String email, String username, String password, String repeatedPassword) {
        //TODO: Handle correctly register data
            registerFormState.setValue(new RegisterFormState(true));

    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isUsernameValid(String username) {
        return username != null && !username.trim().isEmpty();
    }

    private boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile(".*[a-zA-Z].*[0-9].*");
        Matcher matcher = pattern.matcher(password);

        return password.length() > 8 && matcher.matches();
    }

    private boolean isRepeatedPasswordValid(String password, String repeatedPassword) {
        return repeatedPassword.equals(password);
    }

}