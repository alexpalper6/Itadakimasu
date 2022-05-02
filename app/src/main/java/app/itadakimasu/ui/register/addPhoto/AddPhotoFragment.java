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

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.itadakimasu.R;
import app.itadakimasu.databinding.FragmentAddPhotoBinding;
import app.itadakimasu.ui.SelectMediaDialogFragment;


public class AddPhotoFragment extends Fragment {
    // Keys for bundles and magic numbers
    public static final String USERNAME_DISPLAY = "app.itadakimasu.register.addPhoto.usernameDisplay";
    public static final int TAKING_PHOTO_CAMERA = 0;
    public static final int IMAGE_GALLERY = 1;

    private AddPhotoViewModel addPhotoViewModel;
    private FragmentAddPhotoBinding binding;

    // ActivityResult launcher that will handle the permission requests
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startGalleryIntent();
                } else {
                    //TODO: Show in UI why you need the permission
                }
            });

    // ActivityResult launcher that will handle Camera intent and its result
    private ActivityResultLauncher<Uri> photoMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    //TODO: Set image if result is sucessful
                }
            });

    // ActivityResult launcher that will handle Gallery intent and its result
    private ActivityResultLauncher<String> galleryMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                   addPhotoViewModel.setPhotoUri(result);
                }
            });


    public static AddPhotoFragment newInstance() {
        return new AddPhotoFragment();
    }

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
                binding.ivNewPhoto.setImageURI(uri);
            }
        });

        // When clicking the image view, it will display the dialog
        binding.ivNewPhoto.setOnClickListener(v -> {
            DialogFragment dialog= new SelectMediaDialogFragment();
            dialog.show(getParentFragmentManager(), SelectMediaDialogFragment.TAG);

        });

        // Recieves the result of the Dialog using FragmentResultListener
        getParentFragmentManager().setFragmentResultListener(SelectMediaDialogFragment.DIALOG_REQUEST, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int which = result.getInt(SelectMediaDialogFragment.DIALOG_RESULT);
                // Depending on the result, it will open the camera or the gallery
                if (which == TAKING_PHOTO_CAMERA) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    photoMediaLauncher.launch(addPhotoViewModel.getPhotoUri().getValue());

                } else if (which == IMAGE_GALLERY) {
                    checkGalleryPermissions();
                }
            }
        });





    }

    /**
     * TODO: Finish it up
     * Checks for the permissions in order to access the gallery, if the user already granted it,
     * then it will open the gallery, if not, it will show an explanation telling why we request permissions.
     */
    private void checkGalleryPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startGalleryIntent();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

        }
    }

    /**
     * Starts the ActivityResultLauncher for getting content on the gallery
     */
    private void startGalleryIntent() {
        galleryMediaLauncher.launch("image/*");
    }


}