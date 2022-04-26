package app.itadakimasu.ui.register;

import androidx.annotation.Nullable;

import app.itadakimasu.data.model.User;

/**
 * Class that will store the result of the firebase authentication user's registry, that can be
 * the user that has been registered successfully or an error if it failed.
 */
public class RegisterResult {
    @Nullable
    private User user;
    @Nullable
    private String error;

    /**
     * Constructor used to store the user.
     * @param user - the User registered.
     */
    public RegisterResult(@Nullable User user) {
        this.user = user;
    }

    /**
     * Constructor used to store the error.
     * @param error - the error message that Firebase throws.
     */
    public RegisterResult(@Nullable String error) {
        this.error = error;
    }

    /**
     * Obtains the user.
     * @return the User object.
     */
    @Nullable
    public User getUser() {
        return user;
    }

    /**
     * Obtains the error.
     * @return the error message.
     */
    @Nullable
    public String getError() {
        return error;
    }
}
