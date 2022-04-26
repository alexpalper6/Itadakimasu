package app.itadakimasu.ui.register;

import androidx.annotation.Nullable;

import app.itadakimasu.data.model.User;

/**
 * UI State that will hold the error messages that the fragment will use in order to give feedback
 * to the user when the data on the input fields are invalid.
 */
class RegisterFormState {
    // String resources that stores email error message
    @Nullable
    private Integer emailError;
    // String resources that stores username error message
    @Nullable
    private Integer usernameError;
    // String resources that password email error message
    @Nullable
    private Integer passwordError;
    // String resources that stores the repeated password error message
    @Nullable
    private Integer repeatedPasswordError;
    // Boolean that stores true if data input is valid, false if not
    private boolean isDataValid;

    /**
     * Constructor to set different possible errors at once.
     * @param emailError - the email error message from String resources or null
     * @param usernameError - the username error from String resources or null
     * @param passwordError - the password error from String resources or null
     * @param repeatedPasswordError - the repeated from password error String resources or null
     */
    RegisterFormState(@Nullable Integer emailError, @Nullable Integer usernameError, @Nullable Integer passwordError, @Nullable Integer repeatedPasswordError) {
        this.emailError = emailError;
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.repeatedPasswordError = repeatedPasswordError;
        this.isDataValid = false;
    }

    /**
     * Constructor to set only the boolean value.
     * You may use this constructor if you use the below one and want to establish data valid as true
     * and remove error messages from the UI State.
     * @param isDataValid - true if all data is valid, false if not
     */
    RegisterFormState(boolean isDataValid) {
        this.emailError = null;
        this.passwordError = null;
        this.repeatedPasswordError = null;
        this.isDataValid = isDataValid;
    }

    /**
     * Default constructor, set everything at null and isDataValid at false.
     */
    RegisterFormState() {
        this.emailError = null;
        this.usernameError = null;
        this.passwordError = null;
        this.repeatedPasswordError = null;
        this.isDataValid = false;
    }


    /**
     * @return the String resource or null.
     */
    @Nullable
    Integer getEmailError() {
        return emailError;
    }

    /**
     * @return the String resource or null.
     */
    @Nullable
    Integer getUsernameError() {
        return usernameError;
    }

    /**
     * @return the String resource or null.
     */
    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    /**
     * @return the String resource or null.
     */
    @Nullable
    Integer getRepeatedPasswordError() {
        return repeatedPasswordError;
    }

    /**
     * @return true if data is valid; false if not.
     */
    boolean isDataValid() {
        return isDataValid;
    }

    /**
     * Set the String resource message.
     * @param emailError - the email error String resource or null.
     */
    public void setEmailError(@Nullable Integer emailError) {
        this.emailError = emailError;
    }

    /**
     * Set the String resource message.
     * @param usernameError - the username error String resource or null.
     */
    public void setUsernameError(@Nullable Integer usernameError) {
        this.usernameError = usernameError;
    }

    /**
     * Set the String resource message.
     * @param passwordError - the password error String resource or null.
     */
    public void setPasswordError(@Nullable Integer passwordError) {
        this.passwordError = passwordError;
    }

    /**
     * Set the String resource message.
     * @param repeatedPasswordError - the repeated password error String resource or null.
     */
    public void setRepeatedPasswordError(@Nullable Integer repeatedPasswordError) {
        this.repeatedPasswordError = repeatedPasswordError;
    }

    /**
     *  Set the String resource message.
     * @param dataValid - true if data is valid; false if not.
     */
    public void setDataValid(boolean dataValid) {
        this.isDataValid = dataValid;
    }
}
