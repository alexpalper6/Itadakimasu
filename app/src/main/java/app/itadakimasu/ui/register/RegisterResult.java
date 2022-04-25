package app.itadakimasu.ui.register;

import androidx.annotation.Nullable;

import app.itadakimasu.data.model.User;

public class RegisterResult {
    @Nullable
    private User user;
    @Nullable
    private String error;

    public RegisterResult(@Nullable User user) {
        this.user = user;
    }

    public RegisterResult(@Nullable String error) {
        this.error = error;
    }

    @Nullable
    public User getUser() {
        return user;
    }

    @Nullable
    public String getError() {
        return error;
    }
}
