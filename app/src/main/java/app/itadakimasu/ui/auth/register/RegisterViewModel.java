package app.itadakimasu.ui.auth.register;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.User;
import app.itadakimasu.data.repository.AppAuthRepository;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.UsersRepository;

/**
 * RegisterFragment's ViewModel, holds the UI State elements and data required to perform the registry,
 * making them able to survive configuration changes. UI States are stored in order to give the user proper feedback.
 */
public class RegisterViewModel extends AndroidViewModel {
    // Repository used to be able to store user's data on SharedPreferences file.
    private final SharedPrefRepository sharedPrefRepository;
    // Repository that handles the business logic for authentication.
    private final AppAuthRepository authRepository;
    // Repository that handles the business logic for Users data in the database.
    private final UsersRepository usersRepository;
    // Observable and mutable data, UI State that represents the validity of input fields.
    private final MutableLiveData<RegisterFormState> registerFormState;
    // Observable and mutable data, contains the result of the registry.
    private final MutableLiveData<RegisterErrorResult> registerResult;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.authRepository = AppAuthRepository.getInstance();
        this.usersRepository = UsersRepository.getInstance();
        this.registerFormState = new MutableLiveData<>();
        this.registerResult = new MutableLiveData<>();
    }


    /**
     * Returns the register form state.
     * RegisterFormState is a UI state used in RegisterFragment in order to check that the user input is correct.
     *
     * If the input is not valid, the user will get error messages on the input fields. Instead, if it's valid,
     * the register button will be enabled.
     *
     * @return UI state that holds input fields error messages or if every field is valid.
     */
    LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    /**
     * Returns the registerResult observable object, a UI state that contains the user's data after a
     * successful registry or an error.
     *
     * @return a RegisterResult object, that will contain data handled by the fragment, in orther to show
     * an error or to continue with the user creation process.
     */
    LiveData<RegisterErrorResult> getRegisterResult() { return registerResult; }

    /**
     * Registers the user with the input data from the RegisterFragment's fields.
     *
     * @param email - the email that the user has written.
     * @param username - the username that the user has written.
     * @param password - the password that the user has written.
     * @return an observable data of Result class, will contain Firebase's registry result, it can be
     * the user's data if the result was successful or an error message.
     */
    public LiveData<Result<?>> register(String email, String username, String password) {
        return authRepository.register(email, username, password);
    }

    /**
     * Adds the user's data to the database.
     * @param user - the user's data.
     * @return a result, successful if the user could be uploaded; and an error if not.
     */
    LiveData<Result<?>> addUserToDatabase(User user) {
        return usersRepository.addUserToDatabase(user);
    }

    /**
     * If the username result error is sucessfull it means that the error is due that the username is found
     * Instead, is it's a Result.Error, another error happened
     * @param result - the result obtained in order to tell if the user is already chosen or if
     *               another error happened
     */
    public void setUsernameResultError(Result<?> result) {
        if (result instanceof Result.Success) {
            registerFormState.setValue(new RegisterFormState(null, R.string.username_chosen, null, null));
            registerResult.setValue(new RegisterErrorResult(R.string.username_chosen, null, null));
        } else {
            registerResult.setValue(new RegisterErrorResult(null, ((Result.Error) result).getError().getMessage(), null));
        }
    }

    /**
     * Sets the error message when the user creation is not successful.
     * @param errorMessage - the error message.
     */
    public void setRegisterError(String errorMessage) {
        registerResult.setValue(new RegisterErrorResult(null, errorMessage, null));

    }

    /**
     * When the transaction is not successful, the register result will be settled with the error
     * message and the user's data, so the system will notify the user what happened and will allow
     * them to retry.
     * @param message - the error messsage.
     * @param user - the user's data.
     */
    public void setUserUploadError(String message, User user) {
        registerResult.setValue(new RegisterErrorResult(null, message, user));
    }

    /**
     * Every time the user writes input on the register's edit texts fields, this method will catch
     * the content and update the form state.
     *
     * @param email - the email that the user has written.
     * @param username - the username that the user has written.
     * @param password - the password that the user has written.
     * @param repeatedPassword - the seconds password field that the user has written.
     */
    public void registerDataChanged(String email, String username, String password, String repeatedPassword) {
        RegisterFormState newReg = new RegisterFormState();

        boolean isEmailValid = isEmailValid(email);
        boolean isUsernameValid = isUsernameValid(username);
        boolean isPasswordValid = isPasswordValid(password);
        boolean isRepeatedPasswordValid = isRepeatedPasswordValid(password, repeatedPassword);

        if (!isEmailValid) {
            newReg.setEmailError(R.string.invalid_email);
        }
        if (!isUsernameValid) {
            newReg.setUsernameError(R.string.invalid_username);
        }
        if (!isPasswordValid) {
            newReg.setPasswordError(R.string.invalid_password);
        }
        if (!isRepeatedPasswordValid) {
            newReg.setRepeatedPasswordError(R.string.invalid_repeated_password);
        }

        boolean isAllDataValid = isEmailValid && isUsernameValid && isPasswordValid && isRepeatedPasswordValid;
        newReg.setDataValid(isAllDataValid);
        registerFormState.setValue(newReg);

    }

    /**
     *
     * @param username - the username that the user inputs
     * @return a result, a boolean true if username exists, exception if is not found
     */
    public LiveData<Result<?>> isUsernameChosen(String username) {
        return usersRepository.isUsernameChosen(username);
    }

    /**
     * Checks if the email address written by the user is valid.
     * @param email input introduced by user.
     * @return true if email matches an email pattern; false if it doesn't match.
     */
    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Check if the username is not empty.
     * @param username input introduced by user.
     * @return true if the username is not null; false if it's null.
     */
    private boolean isUsernameValid(String username) {
        return username != null && !username.trim().isEmpty();
    }

    /**
     * Checks if the password introduced is greater than 8 letters, and contain characters and numbers.
     * @param password - the input introduced by user.
     * @return true if password is valid; false is is not valid.
     */
    private boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).*$");
        Matcher matcher = pattern.matcher(password);

        return password.length() > 8 && matcher.matches() && password.length() <= 30;
    }

    /**
     * Checks if the repeated password matches the password.
     * @param password - the password input introduced by user.
     * @param repeatedPassword - another password input introduced by user again, must match the previous password.
     * @return true if the repeated password is the same as password; false if it's not.
     */
    private boolean isRepeatedPasswordValid(String password, String repeatedPassword) {
        return !repeatedPassword.trim().isEmpty() && repeatedPassword.equals(password);
    }

    /**
     * Sets the authenticated username on a SharedPreferences.
     * @param username - the username.
     */
    public void setAuthUsername(String username) {
        sharedPrefRepository.setAuthUsername(username);
    }

    /**
     * Sets the authenticated user's photo url on a SharedPreferences.
     * @param photoUrl - the photo's url.
     */
    public void setAuthUserPhotoUrl(String photoUrl) {
        sharedPrefRepository.setAuthUserPhotoUrl(photoUrl);
    }
}