package app.itadakimasu.ui.register.addPhoto;

import androidx.annotation.Nullable;

/**
 * AddPhotoFragment's result state: photoStorageError for image's upload on storage failure.
 */
public class PhotoResultState {
    @Nullable
    private Integer photoStorageError;

    /**
     * Constructor with an integer as parameter.
     * @param photoStorageError - the error message from a String resource.
     */
    public PhotoResultState(@Nullable Integer photoStorageError) {
        this.photoStorageError = photoStorageError;
    }

    /**
     * Default constructor.
     */
    public PhotoResultState() {
        this.photoStorageError = null;
    }

    /**
     * @return the storage failure error message.
     */
    @Nullable
    public Integer getPhotoStorageError() {
        return photoStorageError;
    }


    /**
     * Sets the storage error message. Usually a String resource.
     *
     * @param photoStorageError - the storage failure error message.
     */
    public void setPhotoStorageError(@Nullable Integer photoStorageError) {
        this.photoStorageError = photoStorageError;
    }
}
