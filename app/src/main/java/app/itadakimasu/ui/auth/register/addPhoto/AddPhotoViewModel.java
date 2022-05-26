package app.itadakimasu.ui.auth.register.addPhoto;

import android.app.Application;
import android.net.Uri;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;

/**
 * ViewModel of AddPhotoFragment, stores the data that is necessary to use and result states.
 */
public class AddPhotoViewModel extends AndroidViewModel {
    private final StorageRepository storageRepository;
    private final SharedPrefRepository sharedPrefRepository;
    private final MutableLiveData<String> usernameDisplayState;
    private final MutableLiveData<PhotoResultState> photoResultState;  
    private final MutableLiveData<Uri> photoUriState;
    private final MutableLiveData<String> photoPathState;

    public AddPhotoViewModel(@NonNull Application application) {
        super(application);
        this.storageRepository = StorageRepository.getInstance();
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.usernameDisplayState = new MutableLiveData<>();
        this.photoUriState = new MutableLiveData<>();
        this.photoResultState = new MutableLiveData<>();
        this.photoPathState = new MutableLiveData<>();
    }


    /**
     * @return the username of the user that is given when the user registers from the RegisterFragment.
     */
    public LiveData<String> getDisplayedUsername() {
        return usernameDisplayState;
    }

    /**
     * @return the uri of the image that is given by the user.
     */
    public LiveData<Uri> getPhotoUriState() {
        return photoUriState;
    }

    /**
     * @return the path of the cropped image, used to upload the image to the storage compressed
     */
    public LiveData<String> getPhotoPathState() {
        return photoPathState;
    }

    /**
     * @return the result state when the user tries to upload the photo.
     */
    public LiveData<PhotoResultState> getPhotoResultState() {
        return photoResultState;
    }

    /**
     * Uses the storage repository to upload the image, this method is used with an observer in order
     * to be able to handle the result.
     * @param imageData - the resized image in bytes.
     * @return a result that could be successful if the image is uploaded or not.
     */
    public LiveData<Result<?>> uploadPhotoStorage(byte[] imageData) {

        return storageRepository.updateUserImage(imageData);
    }


    /**
     * Sets the username that will be displayed
     * @param username - username that is received by the Register fragment.
     */
    public void setDisplayedUsername(String username) {
        usernameDisplayState.setValue(username);
    }

    /**
     * Establish the file's uri of the image that the user selects.
     * @param uri - the reference of the file that the user gives by camera or their gallery.
     */
    public void setPhotoUriState(Uri uri) {
        photoUriState.setValue(uri);
    }

    /**
     * Establish the file's uri complete path that permits compress the image.
     * @param photoPath - the file's path
     */
    public void setPhotoPathState(String photoPath) {
        photoPathState.setValue(photoPath);
    }

    /**
     * Establish the error result.
     * @param photoStorageError - the error message.
     */
    public void setUploadPhotoErrorResult(int photoStorageError) {
        photoResultState.setValue(new PhotoResultState(photoStorageError));
    }

    /**
     * @return the current authenticated user's username.
     */
    public String getAuthUsername() {
        return sharedPrefRepository.getAuthUsername();
    }
}
