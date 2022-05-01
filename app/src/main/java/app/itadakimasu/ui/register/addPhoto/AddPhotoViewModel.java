package app.itadakimasu.ui.register.addPhoto;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddPhotoViewModel extends ViewModel {
    private MutableLiveData<String> usernameDisplayState;

    public AddPhotoViewModel() {
        this.usernameDisplayState = new MutableLiveData<>();
    }

    public LiveData<String> getDisplayedUsername() {
        return usernameDisplayState;
    }

    public void setDisplayedUsername(String username) {
        usernameDisplayState.setValue(username);
    }
}
