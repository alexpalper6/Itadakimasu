package app.itadakimasu.ui.auth.login;



/**
 * Class for authentication result, stores the error message.
 */
class LoginErrorResult {
    // Logging error message that will be showed to the user.
    private final String loginError;

    /**
     * Instantiates the login error result with the error message.
     * @param error - the error message.
     */
    LoginErrorResult(String error) {
        this.loginError = error;
    }

    /**
     * @return the error message.
     */
    String getError() {
        return loginError;
    }
}