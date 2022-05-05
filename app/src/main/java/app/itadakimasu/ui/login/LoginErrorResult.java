package app.itadakimasu.ui.login;



/**
 * Authentication result : error message.
 */
class LoginErrorResult {
    private String loginError;

    LoginErrorResult(String error) {
        this.loginError = error;
    }

    String getError() {
        return loginError;
    }
}