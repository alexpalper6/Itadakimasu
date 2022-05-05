package app.itadakimasu.ui.register.addPhoto;

import androidx.annotation.Nullable;

/**
 * AddPhotoFragment's result state: photoUrl and photoUserUrlError for the user's url transaction error
 * and photoStorageError for image's upload on storage failure.
 */
public class PhotoResultState {
    @Nullable
    private Integer photoStorageError;
    @Nullable
    private Integer photoUserUrlError;
    @Nullable
    private String photoUrl;

    public PhotoResultState(@Nullable String photoUrl, @Nullable Integer photoStorageError, @Nullable Integer photoUserUrlError) {
        this.photoUrl = photoUrl;
        this.photoStorageError = photoStorageError;
        this.photoUserUrlError = photoUserUrlError;
    }

    public PhotoResultState() {
        this.photoUrl = null;
        this.photoStorageError = null;
        this.photoUserUrlError = null;
    }

    /**
     * @return the storage failure error message.
     */
    @Nullable
    public Integer getPhotoStorageError() {
        return photoStorageError;
    }

    @Nullable
    public Integer getPhotoUserUrlError() {
        return photoUserUrlError;
    }

    /**
     * @return the photo url.
     */
    @Nullable
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Sets the storage error message.
     * @param photoStorageError - the storage failure error message.
     */
    public void setPhotoStorageError(@Nullable Integer photoStorageError) {
        this.photoStorageError = photoStorageError;
    }

    /**
     * Sets the user's document url upload error message.
     * @param photoUserUrlError - the error message.
     */
    public void setPhotoUserUrlError(@Nullable Integer photoUserUrlError) {
        this.photoUserUrlError = photoUserUrlError;
    }

    /**
     * Sets the photo url given by the storage's photo upload successful result.
     * @param photoUrl - the url reference.
     */
    public void setPhotoUrl(@Nullable String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
