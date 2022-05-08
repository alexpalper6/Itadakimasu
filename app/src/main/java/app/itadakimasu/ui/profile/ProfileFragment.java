package app.itadakimasu.ui.profile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.databinding.FragmentProfileBinding;
import app.itadakimasu.ui.SelectMediaDialogFragment;
import app.itadakimasu.utils.ImageCompressorUtils;
import app.itadakimasu.utils.ImageCropUtils;

public class ProfileFragment extends Fragment {
    public static final int TAKING_PHOTO_CAMERA = 0;
    public static final int IMAGE_GALLERY = 1;
    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private String test;

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
                        profileViewModel.setPhotoUri(resultUri);
                        profileViewModel.setPhotoPath(result.getUriFilePath(requireContext(), false));
                    }
                }
            });


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel notificationsViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textNotifications;
       // notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        binding.btSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
        });

        binding.ivTest.setOnClickListener(v -> {
            DialogFragment dialog = new SelectMediaDialogFragment();
            dialog.show(getParentFragmentManager(), SelectMediaDialogFragment.TAG);

        });
        // Observes changes in the photo uri state from the ViewModel, setting the image uri on the ImageView
        profileViewModel.getPhotoUri().observe(getViewLifecycleOwner(), new Observer<Uri>() {
            @Override
            public void onChanged(Uri uri) {
                Glide.with(requireContext()).load(uri).circleCrop().into(binding.ivTest);
            }
        });

        // Recieves the result of the Dialog using FragmentResultListener
        getParentFragmentManager().setFragmentResultListener(SelectMediaDialogFragment.DIALOG_REQUEST, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // Depending on the result, it will open the camera or the gallery
                int which = result.getInt(SelectMediaDialogFragment.DIALOG_RESULT);

                if (which == TAKING_PHOTO_CAMERA) {
                    photoMediaLauncher.launch(ImageCropUtils.getProfilePictureCameraOptions());
                } else if (which == IMAGE_GALLERY) {

                        checkGalleryPermissions();
                }
            }
        });

        binding.button.setOnClickListener(v -> {
            // Checks if the image is setted by the user
            if (profileViewModel.getPhotoUri().getValue() != null) {
                // Performs the photo uploading.
                uploadPhotoStorageCompressed();
            } else {
                Snackbar.make(
                        requireActivity().findViewById(android.R.id.content)
                        , R.string.add_photo_info
                        , BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

    private void uploadPhotoStorageCompressed() {
        String photoPath = profileViewModel.getPhotoPath().getValue();
        byte[] imageData = ImageCompressorUtils.compressImage(photoPath);

        profileViewModel.uploadPhotoStorage(imageData).observe(getViewLifecycleOwner(), result -> {
            Snackbar.make(requireActivity().findViewById(android.R.id.content), "OK", BaseTransientBottomBar.LENGTH_LONG).show();
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    /**
     * Starts the ActivityResultLauncher for getting content on the gallery
     */
    private void startGalleryIntent() {
        photoMediaLauncher.launch(ImageCropUtils.getProfilePictureGalleryOptions());
    }
}