package app.itadakimasu.ui.myProfile.editProfile;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;

public class EditProfileViewModel extends AndroidViewModel {
    private final StorageRepository storageRepository;
    private final SharedPrefRepository sharedPrefRepository;

    private final MutableLiveData<Uri> photoUri;
    private String photoPath;

    public EditProfileViewModel(@NonNull Application application) {
        super(application);
        this.storageRepository = StorageRepository.getInstance();
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.photoUri = new MutableLiveData<>();
    }

    /**
     * @return observable data of the photo, to show it on the UI.
     */
    public LiveData<Uri> getPhotoUri() {
        return photoUri;
    }

    /**
     * @return result success with the user's photo data from firebase storage; result.error if it fails.
     */
    public LiveData<Result<?>> loadUserPhotoUri() {
        return storageRepository.getImageUri(getAuthUserPhotoUrl());
    }

    /**
     * Uploads the photo to firebase storage.
     * @param imageData - the image's data to upload.
     * @return result.success if it's uploaded successfully; error if else.
     */
    public LiveData<Result<?>> uploadPhotoStorage(byte[] imageData) {
        return storageRepository.updateUserImage(getAuthUserPhotoUrl(), imageData);
    }

    /**
     * @return the photo's path to obtain the bytes.
     */
    public String getPhotoPath() {
        return photoPath;
    }

    /**
     * @return the authenticated user's photo path.
     */
    public String getAuthUserPhotoUrl() {
        return sharedPrefRepository.getAuthUserPhotoUrl();
    }

    /**
     * Sets the uri of the photo to show.
     * @param photoUri - the uri data.
     */
    public void setPhotoUri(Uri photoUri) {
        this.photoUri.setValue(photoUri);
    }

    /**
     * Sets the photo path to compress the image later.
     * @param photoPath - the path were the selected cropped photo is.
     */
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}