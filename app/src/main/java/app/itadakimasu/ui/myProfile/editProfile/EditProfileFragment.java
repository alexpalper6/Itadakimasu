package app.itadakimasu.ui.myProfile.editProfile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.snackbar.Snackbar;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.databinding.FragmentEditProfileBinding;
import app.itadakimasu.utils.ImageCompressorUtils;
import app.itadakimasu.utils.ImageCropUtils;
import app.itadakimasu.utils.dialogs.SelectMediaDialogFragment;

/**
 * Fragment for editing authenticated user's profile data, for now, it only edits the image.
 */
@SuppressWarnings("unchecked")
public class EditProfileFragment extends Fragment {

    private EditProfileViewModel editProfileViewModel;
    private FragmentEditProfileBinding binding;

    // ActivityResultLauncher that will handle the permission requests
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startGalleryIntent();
                } else {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                            R.string.gallery_perm_denied_info, Snackbar.LENGTH_LONG).show();
                }
            });

    // ActivityResultLauncher that will handle Camera or Gallery intent and its result
    private final ActivityResultLauncher<CropImageContractOptions> imageMediaLauncher =
            registerForActivityResult(new CropImageContract(), new ActivityResultCallback<>() {
                @Override
                public void onActivityResult(CropImageView.CropResult result) {
                    if (result.isSuccessful()) {
                        Uri resultUri = result.getUriContent();
                        String resultPath = result.getUriFilePath(requireContext(), false);
                        editProfileViewModel.setPhotoUri(resultUri);
                        editProfileViewModel.setPhotoPath(resultPath);
                    }
                }
            });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editProfileViewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);
        obtainCurrentUserPhoto();

        // Observes for the photo uri obtained from the database or from the user's input.
        editProfileViewModel.getPhotoUri().observe(getViewLifecycleOwner(), photoUri ->
                Glide.with(requireContext()).load(photoUri).circleCrop().error(R.drawable.ic_default_user_profile).into(binding.ivNewPhoto));

        binding.ivNewPhoto.setOnClickListener(v -> {
            DialogFragment dialog = new SelectMediaDialogFragment();
            dialog.show(getParentFragmentManager(), SelectMediaDialogFragment.TAG);
        });

        // Receives the result of the Dialog using FragmentResultListener.
        getParentFragmentManager().setFragmentResultListener(SelectMediaDialogFragment.DIALOG_REQUEST,
                this, (requestKey, result) -> {
                    // Depending on the result, it will open the camera or the gallery
                    int which = result.getInt(SelectMediaDialogFragment.DIALOG_RESULT);

                    if (which == SelectMediaDialogFragment.TAKING_PHOTO_CAMERA) {
                        imageMediaLauncher.launch(ImageCropUtils.getProfilePictureCameraOptions());
                    } else if (which == SelectMediaDialogFragment.IMAGE_GALLERY) {
                        checkGalleryPermissions();
                    }
                });

        // If the clicks done, it will upload the photo on the storage and the user database's info
        binding.btDone.setOnClickListener(v -> {
            // Checks if the image is settled by the user
            if (editProfileViewModel.getPhotoPath() != null) {
                // Performs the photo uploading.
                uploadPhotoStorage();
                binding.pbAddPhoto.setVisibility(View.VISIBLE);
            } else {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });

    }

    /**
     * Uploads the selected photo data to the storage, changing the actual data that it has.
     */
    private void uploadPhotoStorage() {
        String imagePath = editProfileViewModel.getPhotoPath();
        byte[] imageData = ImageCompressorUtils.compressImage(imagePath, ImageCompressorUtils.PROFILE_MAX_HEIGHT, ImageCompressorUtils.PROFILE_MAX_WIDTH);

        editProfileViewModel.uploadPhotoStorage(imageData).observe(getViewLifecycleOwner(), result -> {
            binding.pbAddPhoto.setVisibility(View.GONE);
            if (result instanceof Result.Success) {

                NavHostFragment.findNavController(this).popBackStack();
            } else {
                Snackbar.make(binding.getRoot(), R.string.image_upload_error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Obtains the image as an uri, so the image uri can be stored on the view model.
     * The image url path won't be settled, it will when the user changes the image. This serves
     * as a way to tell if the image is edited, so the app will be able to compress the image.
     */
    private void obtainCurrentUserPhoto() {
        editProfileViewModel.loadUserPhotoUri().observe(getViewLifecycleOwner(), imageResult -> {
            if (imageResult instanceof Result.Success) {
                editProfileViewModel.setPhotoUri(((Result.Success<Uri>) imageResult).getData());
            } else {
                Snackbar.make(binding.getRoot(), R.string.image_load_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Checks for the permissions in order to access the gallery, if the user already granted it,
     * then it will open the gallery, if not, it will show an explanation telling why we request permissions.
     */
    private void checkGalleryPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startGalleryIntent();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

        }
    }

    /**
     * Starts the ActivityResultLauncher for getting content on the gallery.
     */
    private void startGalleryIntent() {
        imageMediaLauncher.launch(ImageCropUtils.getProfilePictureGalleryOptions());
    }
}