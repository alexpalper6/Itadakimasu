package app.itadakimasu.ui.register.addPhoto;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddPhotoViewModel extends ViewModel {
    private MutableLiveData<String> usernameDisplayState;
    private MutableLiveData<Uri> photoUri;

    public AddPhotoViewModel() {
        this.usernameDisplayState = new MutableLiveData<>();
        this.photoUri = new MutableLiveData<>();
    }

    public LiveData<String> getDisplayedUsername() {
        return usernameDisplayState;
    }

    public LiveData<Uri> getPhotoUri() {
        return photoUri;
    }

    public void setDisplayedUsername(String username) {
        usernameDisplayState.setValue(username);
    }

    public void setPhotoUri(Uri uri) {
        photoUri.setValue(uri);
    }
}
