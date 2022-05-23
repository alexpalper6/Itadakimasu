package app.itadakimasu.ui.auth.register.addPhoto;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.databinding.FragmentAddPhotoBinding;
import app.itadakimasu.utils.dialogs.SelectMediaDialogFragment;
import app.itadakimasu.utils.ImageCompressorUtils;
import app.itadakimasu.utils.ImageCropUtils;

/**
 * Fragment that prompts when the user registers, asking them to set their profile's image.
 */
public class AddPhotoFragment extends Fragment {
    private AddPhotoViewModel addPhotoViewModel;
    private FragmentAddPhotoBinding binding;

    // ActivityResultLauncher that will handle the permission requests
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startGalleryIntent();
                } else {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                            R.string.gallery_perm_denied_info, BaseTransientBottomBar.LENGTH_LONG).show();
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
                        addPhotoViewModel.setPhotoUriState(resultUri);
                        addPhotoViewModel.setPhotoPathState(resultPath);
                    }
                }
            });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentAddPhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addPhotoViewModel = new ViewModelProvider(this).get(AddPhotoViewModel.class);
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String retrievedUsername = sharedPreferences.getString(SharedPrefRepository.SAVED_USERNAME_KEY, "");
        addPhotoViewModel.setDisplayedUsername(retrievedUsername);

        // Observes change in the username, so when the fragment gets the username, it will show it on screen.
        addPhotoViewModel.getDisplayedUsername().observe(getViewLifecycleOwner(),
                username -> binding.tvWelcome.setText(getString(R.string.welcome, username)));

        // Observes changes in the photo uri state from the ViewModel, setting the image uri on the ImageView.
        addPhotoViewModel.getPhotoUriState().observe(getViewLifecycleOwner(),
                uri -> Glide.with(requireContext()).load(uri).circleCrop().into(binding.ivNewPhoto));

        // Observes the result of error that could be triggered by a failure uploading an image
        // to the storage.
        addPhotoViewModel.getPhotoResultState().observe(getViewLifecycleOwner(), photoResultState -> {
            binding.pbAddPhoto.setVisibility(View.GONE);
            if (photoResultState.getPhotoStorageError() != null) {
                showStorageErrorSnackbar(photoResultState.getPhotoStorageError());
            }
        });

        // When clicking the image view, it will display the dialog that will let the user
        // to set a photo by camera or gallery.
        binding.ivNewPhoto.setOnClickListener(v -> {
            DialogFragment dialog = new SelectMediaDialogFragment();
            dialog.show(getParentFragmentManager(), SelectMediaDialogFragment.TAG);

        });

        // Receives the result of the Dialog using FragmentResultListener.
        getParentFragmentManager().setFragmentResultListener(SelectMediaDialogFragment.DIALOG_REQUEST,
                this,
                (requestKey, result) -> {
                    // Depending on the result, it will open the camera or the gallery
                    int which = result.getInt(SelectMediaDialogFragment.DIALOG_RESULT);

                    if (which == SelectMediaDialogFragment.TAKING_PHOTO_CAMERA) {
                        imageMediaLauncher.launch(ImageCropUtils.getProfilePictureCameraOptions());
                    } else if (which == SelectMediaDialogFragment.IMAGE_GALLERY) {
                        checkGalleryPermissions();
                    }
                });

        // If the user declines, they will be redirected to the home fragment
        binding.btDecline.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_auth_navigation_to_navigation_home);
        });

        // If the clicks done, it will upload the photo on the storage and the user database's info
        binding.btDone.setOnClickListener(v -> {
            // Checks if the image is setted by the user
            if (addPhotoViewModel.getPhotoUriState().getValue() != null) {
                // Performs the photo uploading.
                uploadPhotoStorage();
                binding.pbAddPhoto.setVisibility(View.VISIBLE);
            } else {
                Snackbar.make(
                        binding.getRoot()
                        , R.string.add_photo_info
                        , BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });

    }


    /**
     * Shows a snackbar with the Storage error message.
     *
     * @param photoStorageError - the error message.
     */
    private void showStorageErrorSnackbar(Integer photoStorageError) {
        Snackbar.make(
                binding.getRoot()
                , photoStorageError
                , BaseTransientBottomBar.LENGTH_LONG
        ).setAction(R.string.retry, v -> uploadPhotoStorage()).show();
    }


    /**
     * With the photo's path obtained from the user image's input, upload to the storage the photo compressed.
     * If the result is successful it will redirect the user to the home section.
     * If it fails, it will set an upload photo error on the result state.
     */
    private void uploadPhotoStorage() {
        String imagePath = addPhotoViewModel.getPhotoPathState().getValue();
        byte[] imageData = ImageCompressorUtils.compressImage(imagePath, ImageCompressorUtils.PROFILE_MAX_HEIGHT, ImageCompressorUtils.PROFILE_MAX_WIDTH);

        addPhotoViewModel.uploadPhotoStorage(imageData).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                binding.pbAddPhoto.setVisibility(View.GONE);
                NavHostFragment.findNavController(this).navigate(R.id.action_auth_navigation_to_navigation_home);
            } else {
                addPhotoViewModel.setUploadPhotoErrorResult(R.string.image_upload_error);
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
     * Starts the ActivityResultLauncher for getting content on the gallery
     */
    private void startGalleryIntent() {
        imageMediaLauncher.launch(ImageCropUtils.getProfilePictureGalleryOptions());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}