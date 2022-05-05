package app.itadakimasu.ui.login;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {
    @Nullable
    private Integer userEmailError;

    private boolean isDataValid;

    LoginFormState(@Nullable Integer usernameError) {
        this.userEmailError = usernameError;
        this.isDataValid = false;
    }

    LoginFormState(boolean isDataValid) {
        this.userEmailError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getUserEmailError() {
        return userEmailError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}