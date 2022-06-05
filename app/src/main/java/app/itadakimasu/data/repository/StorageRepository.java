package app.itadakimasu.data.repository;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.google.firebase.FirebaseException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import app.itadakimasu.data.Result;

/**
 * Repository used to add files on firebase storage.
 */
@SuppressWarnings("unchecked")
public class StorageRepository {
    private static final String TAG = "StorageRepository";
    // Repository's singleton
    public static volatile StorageRepository INSTANCE;
    private final FirebaseStorage dbStorage;

    public static StorageRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (StorageRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StorageRepository();
                }
            }
        }
        return INSTANCE;
    }

    private StorageRepository() {
        this.dbStorage = FirebaseStorage.getInstance();
    }


    /**
     * Given the resized image data, obtains the current user's id and uploads on the Storage
     * a file on the child "users/", this file with have the as name the user's id.
     *
     * It will return a result, successful with the image's path if it's uploaded; error if it fails.
     *
     * @param authUserPhotoUrl - the authenticated user's photo path.
     * @param imageData - the resized image in bytes.
     * @return a result with the image's path on the storage or an error if it fails.
     */
    public LiveData<Result<?>> updateUserImage(String authUserPhotoUrl, byte[] imageData) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "updateUserImage: updating the user's image");
        StorageReference userImage = dbStorage.getReference().child(authUserPhotoUrl);

        userImage.putBytes(imageData)
                .addOnSuccessListener(success -> result.setValue(new Result.Success<String>(userImage.getPath())))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "updateUserImage: error updating the image data", failure);
                    result.setValue(new Result.Error(failure));
                });
        return result;
    }

    /**
     * Updates an image data on the photo url path on the firebase storage.
     *
     * @param photoUrlReference - the photo url path where it will be updated to firebase storage.
     * @param imageData - the image's data in bytes.
     * @return result success with the recipe's path; result error if something wrongs happen.
     */
    public LiveData<Result<?>> updateRecipeImage(String photoUrlReference, byte[] imageData) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "updateRecipeImage: updating recipe's image data");
        StorageReference recipeImage = dbStorage.getReference().child(photoUrlReference);

        recipeImage.putBytes(imageData)
                .addOnSuccessListener(success -> result.setValue(new Result.Success<String>(recipeImage.getPath())))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "updateRecipeImage: error updating recipe's image data", failure);
                    result.setValue(new Result.Error(failure));
                });
        return result;

    }

    /**
     * Deletes a recipe's image from the firebase storage.
     * @param photoUrlReference - the image url path to remove.
     * @return result success if its deleted; result error if something wrongs happen.
     */
    public LiveData<Result<?>> deleteRecipeImage(String photoUrlReference) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "deleteRecipeImage: removing recipe url reference");
        StorageReference reference = dbStorage.getReference().child(photoUrlReference);

        reference.delete().addOnSuccessListener(success -> result.setValue(new Result.Success<Object>(null)))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "deleteRecipeImage: error removing the image reference", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }

    /**
     * Obtains the StorageReference of firebase storage from a given image url path.
     * @param imageUrl - the image url path that will be used to obtain the StorageReference.
     * @return the Storage Reference.
     */
    public StorageReference getImageReference(String imageUrl) {
        return dbStorage.getReference(imageUrl);
    }

    /**
     * Downloads the image data from the firebase Storage, obtaining an image uri.
     * @param imageUrl - the image url that will be used to obtain the uri.
     * @return Result.Success with the image's uri; Result.Error if it fails.
     */
    public LiveData<Result<?>> getImageUri(String imageUrl) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "getImageUri: obtaining image's data as uri");
        dbStorage.getReference(imageUrl).getDownloadUrl().addOnSuccessListener(uri -> result.setValue(new Result.Success(uri)))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "getImageUri: error downloading image's data", failure);
                    if ( ((StorageException) failure).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        result.setValue(new Result.Success<Uri>(null));
                    } else {
                        result.setValue(new Result.Error(failure));
                    }
                });

        return result;
    }
}
