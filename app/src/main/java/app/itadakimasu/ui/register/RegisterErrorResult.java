package app.itadakimasu.ui.register;

import androidx.annotation.Nullable;

import app.itadakimasu.data.model.User;

/**
 * Class that will store the result of the firebase authentication user's registry.
 * The class contains data that will be used in case the registration is not successful:
 *
 * - Integer usernameError: Message from a String resource, used when the user tries to register
 * with a username that already exists.
 *
 * - String error: The error that the firebase returns when the account creation is not successful, or when
 * the user couldn't be added to the database.
 *
 * - User: The user is stored in case the data could not be added to de database, so they could retry
 * to upload it again.
 *
 */
public class RegisterErrorResult {
    @Nullable
    private final Integer usernameError;
    @Nullable
    private final String error;
    @Nullable
    private final User user;

    public RegisterErrorResult(@Nullable Integer usernameError, @Nullable String error, @Nullable User user) {
        this.usernameError = usernameError;
        this.error = error;
        this.user = user;
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

    /**
     * @return - the username error message, usually from a String resource.
     */
    @Nullable
    public Integer getUsernameError() {
        return usernameError;
    }
}
