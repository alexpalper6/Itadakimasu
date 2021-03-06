package app.itadakimasu.ui.recipeCreation;

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
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.databinding.FragmentRecipeCreationBinding;
import app.itadakimasu.utils.ImageCompressorUtils;
import app.itadakimasu.utils.ImageCropUtils;
import app.itadakimasu.utils.dialogs.SelectMediaDialogFragment;
import app.itadakimasu.utils.dialogs.WarningDialogFragment;

/**
 * Main fragment for recipe's creation.
 */
@SuppressWarnings("unchecked")
public class RecipeCreationFragment extends Fragment {
    public static final String REQUEST = "app.itadakimasu.ui.recipeCreation.Request";
    public static final String RESULT = "app.itadakimasu.ui.recipeCreation.Result";

    // Shared view model with the 3 fragments used to add data to the created recipe.
    private CreationViewModel creationViewModel;
    // Binding of the layout.
    private FragmentRecipeCreationBinding binding;

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

                        creationViewModel.setPhotoUri(resultUri);
                        creationViewModel.setPhotoPath(resultPath);

                    }
                }
            });


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Shares ViewModel with the navigation graph.
        NavBackStackEntry backStackEntry = NavHostFragment.findNavController(this).getBackStackEntry(R.id.creation_navigation);
        creationViewModel = new ViewModelProvider(backStackEntry).get(CreationViewModel.class);

        setResultListenerForEdit();

        // Sends to the fragment for adding ingredients to the list
        binding.btAddIngredients.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_recipe_creation_to_navigation_ingredient_creation));

        // Sends to the fragment for adding steps to the list
        binding.btAddSteps.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_recipe_creation_to_navigation_step_Creation));

        // Top button to going back to the home fragment.
        binding.ibGoBack.setOnClickListener(v -> showSureToExitDialog());

        // Opens dialog for adding the image to the recipe.
        binding.ivAddRecipeImage.setOnClickListener(v -> {
            DialogFragment dialog = new SelectMediaDialogFragment();
            dialog.show(getParentFragmentManager(), SelectMediaDialogFragment.TAG);
        });

        // Obtains the data from the edit text and checks that the fields are not empty, then the recipe
        // will start to be uploaded.
        // If 1 of the fields are empty an alert dialog will prompt to the user asking to fill every
        // field.
        binding.fabCreateRecipe.setOnClickListener(v -> {
            String recipeTitle = binding.etAddRecipeTitle.getText().toString().trim();
            String recipeDescription = binding.etAddRecipeDescription.getText().toString().trim();
            if (creationViewModel.isEdited()) {
                if (creationViewModel.areFieldsFilledEdited(recipeTitle, recipeDescription)) {
                    binding.pbProgress.setVisibility(View.VISIBLE);
                    updateRecipe();
                } else {
                    showEmptyFieldsDialog();
                }
            } else {
                if (creationViewModel.areFieldsFilled(recipeTitle, recipeDescription)) {
                    binding.pbProgress.setVisibility(View.VISIBLE);
                    uploadRecipe();
                } else {
                    showEmptyFieldsDialog();
                }
            }
        });

        // Receives the result of the Dialog using FragmentResultListener.
        getParentFragmentManager().setFragmentResultListener(SelectMediaDialogFragment.DIALOG_REQUEST,
                this,
                (requestKey, result) -> {
                    // Depending on the result, it will open the camera or the gallery
                    int which = result.getInt(SelectMediaDialogFragment.DIALOG_RESULT);

                    if (which == SelectMediaDialogFragment.TAKING_PHOTO_CAMERA) {
                        imageMediaLauncher.launch(ImageCropUtils.getRecipePictureCameraOptions());
                    } else if (which == SelectMediaDialogFragment.IMAGE_GALLERY) {
                        checkGalleryPermissions();
                    }
                });

        // Observable to update the Image view with the image that the user loads.
        creationViewModel.getPhotoUri().observe(getViewLifecycleOwner(), imageUri ->
                Glide.with(requireContext()).load(imageUri).centerCrop().error(R.drawable.ic_baseline_image_not_supported_24).into(binding.ivAddRecipeImage));

    }


    /**
     * Sets a fragment result listener, this will get a recipe in order to edit it.
     * If this fragment is going to be used to edit a recipe, the buttons for adding
     * ingredients and steps will be disabled.
     */
    private void setResultListenerForEdit() {
        getParentFragmentManager().setFragmentResultListener(REQUEST, this, (requestKey, result) -> {

            Recipe recipe = result.getParcelable(RESULT);
            creationViewModel.setEdited(true);
            creationViewModel.setRecipeIdToEdit(recipe.getId());
            creationViewModel.setRecipeDateToEdit(recipe.getCreationDate());

            binding.etAddRecipeTitle.setText(recipe.getTitle());
            binding.etAddRecipeDescription.setText(recipe.getDescription());

            binding.btAddIngredients.setEnabled(false);
            binding.btAddSteps.setEnabled(false);

            // Obtains the image as an uri, so the image uri can be stored on the view model.
            // The image url path won't be settled, it will when the user changes the image. This serves
            // as a way to tell if the image is edited, so the app will be able to compress the image.
            creationViewModel.getEditedRecipeImage(recipe.getPhotoUrl()).observe(getViewLifecycleOwner(), imageResult -> {
                if (imageResult instanceof Result.Success) {
                    creationViewModel.setPhotoUri(((Result.Success<Uri>) imageResult).getData());
                } else {
                    Snackbar.make(binding.getRoot(), R.string.image_load_error, Snackbar.LENGTH_LONG).show();
                }
            });


        });
    }

    /**
     * Obtains the user's data from the shared preferences and uploads the recipe to the database.
     */
    private void uploadRecipe() {
        String username = creationViewModel.getAuthUsername();
        String userPhotoUrl = creationViewModel.getAuthUserPhotoUrl();

        // If the username or the user's photo url is empty then a snack bar will prompt to the user telling
        // the user that theirs data couldn't be uploaded.

        // Then the upload wont be performed, because the recipe must contain the username and user's image.
        if (username.length() == 0 || userPhotoUrl.length() == 0) {
            Snackbar.make(binding.getRoot(), R.string.user_data_retrieve_error, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.fabCreateRecipe).show();
            binding.pbProgress.setVisibility(View.GONE);
            return;
        } 

        // View model's method to upload the recipe. Observes for the result.
        // If the result is successful then the recipe's photo will be uploaded to the Storage.
        creationViewModel.uploadRecipe(username, userPhotoUrl, binding.etAddRecipeTitle.getText().toString(), binding.etAddRecipeDescription.getText().toString())
                .observe(getViewLifecycleOwner(), result -> {
                    if (result instanceof Result.Success) {
                        uploadPhotoStorage(((Result.Success<String>) result).getData());
                    } else {
                        // If the uploading is not successful a snack bar will prompt to the user.
                        // The user will be able to retry the upload.
                        Snackbar.make(binding.getRoot(), R.string.recipe_upload_error, Snackbar.LENGTH_LONG)
                                .setAnchorView(binding.fabCreateRecipe).setAction(R.string.retry, v -> uploadRecipe()).show();
                    }
                });
    }

    /**
     * Uploads the recipe's image to the firebase storage.
     * @param recipePhotoUrl - the recipe's url image where the image's data will be uploaded.
     */
    private void uploadPhotoStorage(String recipePhotoUrl) {
        // Obtains the path of the recipe's cropped image and compress it.
        String imagePath = creationViewModel.getPhotoPath();
        byte[] imageData = ImageCompressorUtils.compressImage(imagePath, ImageCompressorUtils.LANDSCAPE_MAX_HEIGHT, ImageCompressorUtils.LANDSCAPE_MAX_WIDTH);

        // View model method to upload the photo to storage.
        // Observes for a result, if the result is successful the user will be sent back to the host fragment.
        creationViewModel.uploadPhotoStorage(recipePhotoUrl, imageData).observe(getViewLifecycleOwner(), result -> {
            binding.pbProgress.setVisibility(View.GONE);
            if (result instanceof Result.Success) {
                NavHostFragment.findNavController(this).popBackStack();

            } else {
                // If the upload fails the user will be informed via a snack bar, they will be able to retry to upload it again.
                Snackbar.make(binding.getRoot(), R.string.image_upload_error, Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.fabCreateRecipe).setAction(R.string.retry, v -> uploadPhotoStorage(recipePhotoUrl)).show();
            }
        });
    }

    /**
     * Updates an existed recipe, used when its edited.
     */
    private void updateRecipe() {
        String username = creationViewModel.getAuthUsername();
        String userPhotoUrl = creationViewModel.getAuthUserPhotoUrl();

        // If the username or the user's photo url is empty then a snack bar will prompt to the user telling
        // the user that theirs data couldn't be uploaded.

        // Then the upload wont be performed, because the recipe must contain the username and user's image.
        if (username.length() == 0 || userPhotoUrl.length() == 0) {
            Snackbar.make(binding.getRoot(), R.string.user_data_retrieve_error, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.fabCreateRecipe)
                    .show();
            binding.pbProgress.setVisibility(View.GONE);
            return;
        }

        creationViewModel.updateRecipe(username, userPhotoUrl, binding.etAddRecipeTitle.getText().toString(),
                binding.etAddRecipeDescription.getText().toString()).observe(getViewLifecycleOwner(), result -> {

                    if (result instanceof Result.Success) {
                        // Checks if the image was edited by the user.
                        //This serves as a way to tell if the image is edited, so the app will be able to compress the image.
                        if (creationViewModel.getPhotoPath() != null) {
                            uploadPhotoStorage(((Result.Success<String>) result).getData());
                        } else {
                            NavHostFragment.findNavController(this).popBackStack();
                        }
                    } else {
                        // If the updating is not successful a snack bar will prompt to the user.
                        // The user will be able to retry the update.
                        Snackbar.make(binding.getRoot(), R.string.recipe_update_error, Snackbar.LENGTH_LONG)
                                .setAnchorView(binding.fabCreateRecipe).setAction(R.string.retry, v -> updateRecipe()).show();
                    }
                });
    }

    /**
     * Prompts an Alert Dialog to the user, telling that the recipe has empty fields.
     */
    private void showEmptyFieldsDialog() {
        WarningDialogFragment dialog = new WarningDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(WarningDialogFragment.DIALOG_TITLE, R.string.fields_empty);
        bundle.putInt(WarningDialogFragment.DIALOG_MESSAGE, R.string.fill_every_field);

        dialog.setArguments(bundle);

        dialog.show(getParentFragmentManager(), WarningDialogFragment.TAG);
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
        imageMediaLauncher.launch(ImageCropUtils.getRecipePictureGalleryOptions());
    }

    private void showSureToExitDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.exit_without_creating)
                .setMessage(R.string.recie_data_lost_warning)
                .setPositiveButton(R.string.exit, (dialog, which) -> NavHostFragment.findNavController(this).popBackStack())
                .setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.cancel()));

        builder.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}