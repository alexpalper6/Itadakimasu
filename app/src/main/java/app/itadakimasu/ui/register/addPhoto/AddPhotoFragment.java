package app.itadakimasu.ui.register.addPhoto;

import android.Manifest;
import android.content.Intent;
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
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.MediaStore;
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
import app.itadakimasu.databinding.FragmentAddPhotoBinding;
import app.itadakimasu.ui.SelectMediaDialogFragment;
import app.itadakimasu.utils.ImageCropUtils;


public class AddPhotoFragment extends Fragment {
    // Keys for bundles and magic numbers
    public static final String USERNAME_DISPLAY = "app.itadakimasu.register.addPhoto.usernameDisplay";
    public static final int TAKING_PHOTO_CAMERA = 0;
    public static final int IMAGE_GALLERY = 1;

    private AddPhotoViewModel addPhotoViewModel;
    private FragmentAddPhotoBinding binding;

    // ActivityResultLauncher that will handle the permission requests
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startGalleryIntent();
                } else {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                            R.string.gallery_perm_denied_info, BaseTransientBottomBar.LENGTH_LONG).show();
                }
            });

    // ActivityResultLauncher that will handle Camera intent and its result
    private ActivityResultLauncher<CropImageContractOptions> photoMediaLauncher =
            registerForActivityResult(new CropImageContract(), new ActivityResultCallback<CropImageView.CropResult>() {
                @Override
                public void onActivityResult(CropImageView.CropResult result) {
                    if (result.isSuccessful()) {
                        Uri resultUri = result.getUriContent();
                        addPhotoViewModel.setPhotoUri(resultUri);
                    }
                }
            });

    // ActivityResultLauncher that will handle the Gallery intent selection and its result
    private ActivityResultLauncher<CropImageContractOptions> galleryMediaLauncher =
            registerForActivityResult(new CropImageContract(), new ActivityResultCallback<CropImageView.CropResult>() {
                @Override
                public void onActivityResult(CropImageView.CropResult result) {
                    if (result.isSuccessful()) {
                        Uri resultUri = result.getUriContent();
                        addPhotoViewModel.setPhotoUri(resultUri);
                    }
                }
            });


    /*public static AddPhotoFragment newInstance() {
        return new AddPhotoFragment();
    }*/

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
        addPhotoViewModel.setDisplayedUsername(getArguments().getString(USERNAME_DISPLAY));

        // Observes change in the username, so when the fragment gets the username, it will show it on screen
        addPhotoViewModel.getDisplayedUsername().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String username) {
                binding.tvWelcome.setText(getString(R.string.welcome, username));
            }
        });

        // Observes changes in the photo uri state from the ViewModel, setting the image uri on the ImageView
        addPhotoViewModel.getPhotoUri().observe(getViewLifecycleOwner(), new Observer<Uri>() {
            @Override
            public void onChanged(Uri uri) {
                Glide.with(requireContext()).load(uri).circleCrop().into(binding.ivNewPhoto);
            }
        });

        // Observes the result of errors that could be triggered by a failure uploading an image
        // to the storage or when updating image user's url
        addPhotoViewModel.getPhotoResultState().observe(getViewLifecycleOwner(), photoResultState -> {
            if (photoResultState.getPhotoStorageError() != null) {
                showStorageErrorSnackbar(photoResultState.getPhotoStorageError());
                return;
            }

            if (photoResultState.getPhotoUserUrlError() != null) {
                showPhotoUserErrorSnackbar(photoResultState.getPhotoUserUrlError(), photoResultState.getPhotoUrl());
            }
        });

        // When clicking the image view, it will display the dialog that will let the user
        // to set a photo by camera or gallery
        binding.ivNewPhoto.setOnClickListener(v -> {
            DialogFragment dialog = new SelectMediaDialogFragment();
            dialog.show(getParentFragmentManager(), SelectMediaDialogFragment.TAG);

        });

        // Recieves the result of the Dialog using FragmentResultListener
        getParentFragmentManager().setFragmentResultListener(SelectMediaDialogFragment.DIALOG_REQUEST, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // Depending on the result, it will open the camera or the gallery
                int which = result.getInt(SelectMediaDialogFragment.DIALOG_RESULT);

                if (which == TAKING_PHOTO_CAMERA) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    photoMediaLauncher.launch(ImageCropUtils.getProfilePictureCameraOptions());
                } else if (which == IMAGE_GALLERY) {
                    checkGalleryPermissions();
                }
            }
        });

        // If the user declines, they will be redirected to the home fragment
        binding.btDecline.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_photo_addition_to_navigation_home);
        });

        // If the clicks done, it will upload the photo on the storage and the user database's info
        binding.btDone.setOnClickListener(v -> {
            // Checks if the image is setted by the user
            if (addPhotoViewModel.getPhotoUri().getValue() != null) {
                addPhotoViewModel.uploadPhotoStorage(addPhotoViewModel.getPhotoUri().getValue());
            } else {
                Snackbar.make(
                        requireActivity().findViewById(android.R.id.content)
                        , R.string.add_photo_info
                        , BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });

    }


    /**
     * Shows a snackbar with the Storage error message.
     * @param photoStorageError - the error message.
     */
    private void showStorageErrorSnackbar(Integer photoStorageError) {
        Snackbar.make(
                requireActivity().findViewById(android.R.id.content)
                , photoStorageError
                , BaseTransientBottomBar.LENGTH_LONG
        ).setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhotoStorage();
            }
        }).show();
    }

    /**
     * Shows a snackbar with the error that is received when the image's url update on the data failed.
     * @param photoUserUrlError - the photoResult that holds the error images and url.
     * @param photoUrl - the reference of the image.
     */
    private void showPhotoUserErrorSnackbar(Integer photoUserUrlError, String photoUrl) {
        Snackbar.make(
                requireActivity().findViewById(android.R.id.content)
                , photoUserUrlError
                , BaseTransientBottomBar.LENGTH_LONG
        ).setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completePhotoTransaction(photoUrl);
            }
        }).show();
    }

    /**
     * When the photo is uploaded correctly on storage, then the url path that references the image
     * will be stored in the user's data.
     * @param photoUrl - image reference.
     */
    private void completePhotoTransaction(String photoUrl) {
        addPhotoViewModel.updateUserPhotoUrl(photoUrl).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Error) {
                addPhotoViewModel.setUserUrlTransactionError(R.string.image_user_error, photoUrl);
            } else {
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_photo_addition_to_navigation_home);
            }
        });
    }

    /**
     * With the photoUri obtained from the user image's input, upload to the storage the photo.
     * If the result is sucessfull it will upload the photo url to the user's data on the database.
     * If it fails, it will set an upload photo error on the result state.
     */
    private void uploadPhotoStorage() {
        Uri uri = addPhotoViewModel.getPhotoUri().getValue();

        addPhotoViewModel.uploadPhotoStorage(uri).observe(getViewLifecycleOwner(), new Observer<Result<?>>() {
            @Override
            public void onChanged(Result<?> result) {
                if (result instanceof Result.Success) {
                    completePhotoTransaction(((Result.Success<String>) result).getData());
                } else {
                    addPhotoViewModel.setUploadPhotoErrorResult(R.string.image_upload_error);
                }
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
        galleryMediaLauncher.launch(ImageCropUtils.getProfilePictureGalleryOptions());
    }
}