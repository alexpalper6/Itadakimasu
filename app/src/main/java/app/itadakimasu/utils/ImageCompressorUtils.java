package app.itadakimasu.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Class util that resize and compress the image so it's file size is lighter when uploaded to the server.
 * Credit to Jitty Andyan for posting the method to resize and compress the image.
 * https://stackoverflow.com/questions/28424942/decrease-image-size-without-losing-its-quality-in-android
 */
public class ImageCompressorUtils {
    public static float PROFILE_MAX_HEIGHT = 400.0f;
    public static float PROFILE_MAX_WIDTH= 400.0f;
    public static float LANDSCAPE_MAX_HEIGHT= 566.0f;
    public static float LANDSCAPE_MAX_WIDTH= 1080.0f;

    /**
     * Compress an image
     * @param imageUri - the path of the image
     * @return the bytes of the image that will be uploaded to the server.
     */
    public static byte[] compressImage(String imageUri, float maxHeight, float maxWidth) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        // By setting this field as true, the actual bitmap pixels are not loaded in the memory.
        // Just the bounds are loaded. If you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imageUri, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        // We get the image ratio and the max ratio permitted dividing the width with the height.
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // Width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;

            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;

            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        // Setting inSampleSize value allows to load a scaled down version of the original image.
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        // InJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        // This options allow android to claim the bitmap memory if it runs low on memory
        options.inBitmap = bmp;
        options.inTempStorage = new byte[16 * 1024];

        try {
            // Load the bitmap from its path
            bmp = BitmapFactory.decodeFile(imageUri, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }

        Bitmap scaledBitmap = null;

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        // Check the rotation of the image and display it properly.
        ExifInterface exif;
        try {
            exif = new ExifInterface(imageUri);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] compressedImageData = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            compressedImageData = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return compressedImageData;

    }

    /**
     * Calculates the sample size that will allow to laad a scaled down version of the original image.
     * @param options - BitmapFactory options that holds the bounds of the image
     * @param reqWidth - the required resized width that will calculate part the sampleSize with the original
     *                 image's width
     * @param reqHeight - the required resized height that will calculate part of the sampleSize with the original
     *                  imagei's height
     * @return the sample size number coeficient that will let the BitmapFactory scale the image
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        // Calculate the sampleSize number coeficient, if the height of the width if greater than the
        // resized ones, they will get divided by the resized.
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.min(heightRatio, widthRatio);
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }


}
