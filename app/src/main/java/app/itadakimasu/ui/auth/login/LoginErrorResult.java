package app.itadakimasu.ui.auth.login;



/**
 * Authentication result : error message.
 */
class LoginErrorResult {
    private final String loginError;

    LoginErrorResult(String error) {
        this.loginError = error;
    }

    String getError() {
        return loginError;
    }
}