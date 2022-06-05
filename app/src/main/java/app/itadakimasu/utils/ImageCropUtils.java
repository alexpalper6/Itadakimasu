package app.itadakimasu.utils;

import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import app.itadakimasu.R;

/**
 * Util class that uses Android-Image-Cropper to set crop image options.
 */
public class ImageCropUtils {
    /**
     * Sets the crop area as an oval for images from the gallery.
     * @return options for cropping a profile image from gallery.
     */
    public static CropImageContractOptions getProfilePictureGalleryOptions() {
        return new CropImageContractOptions(null, new CropImageOptions())
                .setCropMenuCropButtonIcon(R.drawable.ic_baseline_done_crop_24)
                .setImageSource(true, false)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setScaleType(CropImageView.ScaleType.FIT_CENTER);
    }

    /**
     * Sets the crop area as an oval for images taken from the camera.
     * @return options for cropping a profile image from camera.
     */
    public static CropImageContractOptions getProfilePictureCameraOptions() {
        return new CropImageContractOptions(null, new CropImageOptions())
                .setCropMenuCropButtonIcon(R.drawable.ic_baseline_done_crop_24)
                .setImageSource(false, true)
                .setFixAspectRatio(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setScaleType(CropImageView.ScaleType.FIT_CENTER);
    }

    /**
     * Sets the crop area as a rectangle for images from the gallery.
     * @return options for cropping a recipe image from gallery.
     */
    public static CropImageContractOptions getRecipePictureGalleryOptions() {
        return new CropImageContractOptions(null, new CropImageOptions())
                .setCropMenuCropButtonIcon(R.drawable.ic_baseline_done_crop_24)
                .setImageSource(true, false)
                .setFixAspectRatio(false)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setScaleType(CropImageView.ScaleType.FIT_CENTER);
    }

    /**
     * Sets the crop area as a rectangle for images taken from camera.
     * @return options for cropping a recipe image from gallery.
     */
    public static CropImageContractOptions getRecipePictureCameraOptions() {
        return new CropImageContractOptions(null, new CropImageOptions())
                .setCropMenuCropButtonIcon(R.drawable.ic_baseline_done_crop_24)
                .setImageSource(false, true)
                .setFixAspectRatio(false)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setScaleType(CropImageView.ScaleType.FIT_CENTER);
    }
}
