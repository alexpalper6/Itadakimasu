package app.itadakimasu.ui.auth.login;

import androidx.annotation.Nullable;

/**
 * Data state of the login form, this is used when the user input text on the fields,
 * enables login button when data is valid, shows email error if the email is not valid.
 */
class LoginFormState {
    // The email error message stored on string resources in case the user inputs a non-valid email on the email field.
    @Nullable
    private final Integer userEmailError;
    // Boolean state used to enable the login button, false when the fields are empty and not valid;
    // true when fields are not empty and are valid.
    private final boolean isDataValid;

    /**
     * Instantiates login form state with the user email error message.
     * @param userEmailError - the email error message that is stored on string resources, used to show the
     *                       error message to the user.
     */
    LoginFormState(@Nullable Integer userEmailError) {
        this.userEmailError = userEmailError;
        this.isDataValid = false;
    }

    /**
     * Instantiates the login form state with a data valid value, normally this is used to set it to true.
     * @param isDataValid - true if data is valid; false if not. If it's true login button will be enabled.
     */
    LoginFormState(boolean isDataValid) {
        this.userEmailError = null;
        this.isDataValid = isDataValid;
    }

    /**
     * @return the email error message.
     */
    @Nullable
    Integer getUserEmailError() {
        return userEmailError;
    }

    /**
     * @return the data's state: true if is valid; false if not.
     */
    boolean isDataValid() {
        return isDataValid;
    }
}