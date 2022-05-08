package app.itadakimasu.data.repository;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;

/**
 * Repository used to add files on the Storage.
 */
public class StorageRepository {
    private FirebaseStorage dbStorage;
    private FirebaseAuth authInstance;

    public StorageRepository() {
        this.dbStorage = FirebaseStorage.getInstance();
        this.authInstance = FirebaseAuth.getInstance();
    }


    /**
     * Given the resized image data, obtains the current user and uploads on the Storage
     * a file on the child "users/", this file with have the as name the user's username (example: alexpalper.jpeg).
     *
     * It will return a result, successful with the image's path if it's uploaded; error if it fails.
     * @param imageData - the resized image in bytes.
     * @return a result with the image's path on the storage or an error if it fails.
     */
    public LiveData<Result<?>> updateUserImage(byte[] imageData) {
        String userId = Objects.requireNonNull(authInstance.getCurrentUser()).getUid();
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        StorageReference userImage = dbStorage.getReference().child(FirebaseContract.StorageReference.USER_PICTURES + userId);


        userImage.putBytes(imageData)
                .addOnSuccessListener(success -> result.setValue(new Result.Success<String>(userImage.getPath())))
                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));
        return result;
    }
}
