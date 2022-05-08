package app.itadakimasu.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.repository.StorageRepository;


public class ProfileViewModel extends ViewModel {


    private final StorageRepository storageRepository;
    private final MutableLiveData<Uri> photoUri;
    private MutableLiveData<String> photoPath;
    public ProfileViewModel() {
        this.storageRepository = new StorageRepository();
        this.photoUri = new MutableLiveData<>();
    }

    public LiveData<Uri> getPhotoUri() {
        return photoUri;
    }
    public void setPhotoUri(Uri uri) {
        photoUri.setValue(uri);
    }

    public LiveData<String> getPhotoPath() {
        return photoPath;
    }
    public void setPhotoPath(String photoUrl) {
        photoPath.setValue(photoUrl);
    }

    public LiveData<Result<?>> uploadPhotoStorage(byte[] imageData) {

        return storageRepository.updateUserImage(imageData);
    }

}