package app.itadakimasu.utils;

import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

/**
 * Util class that uses Android-Image-Cropper to set crop image options.
 */
public class ImageCropUtils {

    public static CropImageContractOptions getProfilePictureGalleryOptions() {
        return new CropImageContractOptions(null, new CropImageOptions())
                .setImageSource(true, false)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setScaleType(CropImageView.ScaleType.FIT_CENTER);
    }

    public static CropImageContractOptions getProfilePictureCameraOptions() {
        return new CropImageContractOptions(null, new CropImageOptions())
                .setImageSource(false, true)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setScaleType(CropImageView.ScaleType.FIT_CENTER);
    }
}
