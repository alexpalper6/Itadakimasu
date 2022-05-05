package app.itadakimasu.ui.register.addPhoto;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.repository.StorageRepository;
import app.itadakimasu.data.repository.UsersRepository;

/**
 * ViewModel of AddPhotoFragment, stores the data that is necessary to use and result states.
 */
public class AddPhotoViewModel extends ViewModel {
    private UsersRepository usersRepository;
    private StorageRepository storageRepository;
    private MutableLiveData<String> usernameDisplayState;
    private MutableLiveData<Uri> photoUri;
    private MutableLiveData<PhotoResultState> photoResultState;

    public AddPhotoViewModel() {
        this.usersRepository = new UsersRepository();
        this.storageRepository = new StorageRepository();
        this.usernameDisplayState = new MutableLiveData<>();
        this.photoUri = new MutableLiveData<>();
        this.photoResultState = new MutableLiveData<>();
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
    public LiveData<Uri> getPhotoUri() {
        return photoUri;
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
     * @param uriImage - the file path where the user has the image.
     * @return a result that could be successful if the image is uploaded or not.
     */
    public LiveData<Result<?>> uploadPhotoStorage(Uri uriImage) {
        return storageRepository.updateUserImage(uriImage);
    }

    /**
     * Set the photoUrl of the User's document on the database.
     * @param photoUrl - the photo's reference on the storage.
     * @return a result with an error that won't be null if the update fails.
     */
    public LiveData<Result<Result.Error>> updateUserPhotoUrl(String photoUrl) {
        return usersRepository.updateUserPhotoUrl(photoUrl);
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
    public void setPhotoUri(Uri uri) {
        photoUri.setValue(uri);
    }

    /**
     * Establish the error result.
     * @param photoStorageError - the error message.
     */
    public void setUploadPhotoErrorResult(int photoStorageError) {
        photoResultState.setValue(new PhotoResultState(null, photoStorageError, null));
    }

    /**
     * Sets the error when the transaction uploading on the user's document the photo url fails.
     * @param photoUrlUserError - the message.
     * @param photoUrl - the photo's url that references the image on the storage.
     */
    public void setUserUrlTransactionError(int photoUrlUserError, String photoUrl) {
        photoResultState.setValue(new PhotoResultState(photoUrl, null, photoUrlUserError));
    }

}
