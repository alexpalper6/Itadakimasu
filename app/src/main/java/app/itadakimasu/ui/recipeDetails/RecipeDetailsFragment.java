package app.itadakimasu.ui.recipeDetails;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.model.Step;
import app.itadakimasu.databinding.FragmentRecipeDetailsBinding;
import app.itadakimasu.ui.adapters.RecipeDetailsIngredientAdapter;
import app.itadakimasu.ui.adapters.RecipeDetailsStepAdapter;
import app.itadakimasu.ui.userProfile.UserProfileFragment;

/**
 * Fragments to see the details of a recipe.
 */
@SuppressWarnings("unchecked")
public class RecipeDetailsFragment extends Fragment {
    // Constant for obtaining a recipe's data result from fragment requests.
    public static final String REQUEST = "app.itadakimasu.ui.recipeDetails.Request";
    public static final String RESULT = "app.itadakimasu.ui.recipeDetails.Result";

    // Binding for recipe details layout.
    private FragmentRecipeDetailsBinding binding;
    // View model that stores the recipe's data on memory for configuration changes survival.
    private RecipeDetailsViewModel detailsViewModel;

    // Adapter for the list of ingredients and steps.
    private RecipeDetailsIngredientAdapter ingredientsAdapter;
    private RecipeDetailsStepAdapter stepsAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        detailsViewModel = new ViewModelProvider(this).get(RecipeDetailsViewModel.class);
        setupRecyclerViews();
        // Obtains the recipe to show the details from another fragment as a result.
        getParentFragmentManager().setFragmentResultListener(REQUEST, this, (requestKey, result) -> {
            Recipe recipe = result.getParcelable(RESULT);
            detailsViewModel.setSelectedRecipe(recipe);
        });

        // When the recipe is obtained, shows the data.
        detailsViewModel.getSelectedRecipe().observe(getViewLifecycleOwner(), this::showRecipeData);

        // Submit the list of ingredients and lists to the adapters.
        detailsViewModel.getIngredientList().observe(getViewLifecycleOwner(), ingredientList -> ingredientsAdapter.submitList(ingredientList));
        detailsViewModel.getStepList().observe(getViewLifecycleOwner(), stepList -> stepsAdapter.submitList(stepList));

        // When selecting the author's image or username, the user is sent to their profile.
        binding.ivUserImage.setOnClickListener(v -> goToAuthorProfile());
        binding.tvAuthor.setOnClickListener(v -> goToAuthorProfile());
        // When clicking to the go back button, the user is sent back to the previous fragment where they were.
        binding.ibGoBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        // Adds or removes from favourite the recipe.
        binding.cbFavourite.setOnClickListener(v -> {
            if (detailsViewModel.isRecipeFavourite()) {
                removeFromFavourites();
            } else {
                addToFavourites();
            }
        });

    }

    /**
     * The recipe's data except the lists are obtained with the recipe and loaded into its views.
     * After that, the ingredients and steps are loaded using the recipe's id.
     * @param recipe - the recipe from which data is loaded.
     */
    private void showRecipeData(Recipe recipe) {
        // If the author is the same as the authenticated user, then they wont be able to add the recipe
        // to favourites.
        if (recipe.getAuthor().equals(detailsViewModel.getAuthUsername())) {
            binding.cbFavourite.setVisibility(View.GONE);
        }
        binding.cbFavourite.setEnabled(true);

        loadAuthorImage(recipe.getPhotoAuthorUrl());
        loadRecipeImage(recipe.getPhotoUrl());

        binding.tvAuthor.setText(getString(R.string.created_by, recipe.getAuthor()));
        binding.tvRecipeTitle.setText(recipe.getTitle());
        binding.tvRecipeDescription.setText(recipe.getDescription());
        // Checks if the recipe is a user's favourite one.
        loadRecipeFavourite();
        // Loads the ingredient and step list.
        loadIngredientList(recipe.getId());
        loadStepList(recipe.getId());

    }

    /**
     * Downloads the recipe image and updates the UI.
     * @param photoUrl - the recipe's image url.
     */
    private void loadRecipeImage(String photoUrl) {
        detailsViewModel.downloadImageData(photoUrl).observe(getViewLifecycleOwner(), result -> {
           if (result instanceof Result.Success) {
               Uri uriImage = ((Result.Success<Uri>) result).getData();
               Glide.with(requireContext()).load(uriImage).centerCrop().into(binding.ivRecipeImage);
           } else {
               Snackbar.make(binding.getRoot(), R.string.image_load_error, Snackbar.LENGTH_LONG)
                       .setAction(R.string.retry, v -> loadRecipeImage(photoUrl))
                       .show();
           }
        });
    }

    /**
     * Downloads the author photo and updates the UI.
     * @param photoAuthorUrl - the author's image url.
     */
    private void loadAuthorImage(String photoAuthorUrl) {
        detailsViewModel.downloadImageData(photoAuthorUrl).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                Uri uriImage = ((Result.Success<Uri>) result).getData();
                Glide.with(requireContext()).load(uriImage).circleCrop().into(binding.ivUserImage);

            } else {
                Snackbar.make(binding.getRoot(), R.string.image_load_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> loadAuthorImage(photoAuthorUrl))
                        .show();
            }
        });
    }

    /**
     * Checks if the user added to favourites the selected recipe.
     */
    private void loadRecipeFavourite() {
        detailsViewModel.loadRecipeAsFavourite().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                boolean isFavourite = ((Result.Success<Boolean>) result).getData();
                binding.cbFavourite.setChecked(isFavourite);
                detailsViewModel.setRecipeAsFavourite(isFavourite);
            }
        });
    }

    /**
     * Adds the selected recipe to the user's favourites.
     */
    private void addToFavourites() {
        detailsViewModel.addRecipeToFavourites().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                binding.cbFavourite.setChecked(true);
                detailsViewModel.setRecipeAsFavourite(true);
            } else {
                binding.cbFavourite.setChecked(false);
                Snackbar.make(binding.getRoot(), R.string.add_fav_error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Removes the recipe from the user's favourites.
     */
    private void removeFromFavourites() {
        detailsViewModel.removeRecipeFromFavourites().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                binding.cbFavourite.setChecked(false);
                detailsViewModel.setRecipeAsFavourite(false);
            } else {
                binding.cbFavourite.setChecked(false);
                Snackbar.make(binding.getRoot(), R.string.remove_fav_error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Obtains the recipe author's data and sends the user to the author's profile fragment, sending
     * the author's data as a fragment result.
     */
    private void goToAuthorProfile() {
        Bundle result = new Bundle();
        // Obtains the recipe and author's data.
        Recipe userRecipe = detailsViewModel.getSelectedRecipe().getValue();
        result.putString(UserProfileFragment.RESULT_USERNAME, userRecipe.getAuthor());
        result.putString(UserProfileFragment.RESULT_USER_PHOTO, userRecipe.getPhotoAuthorUrl());
        // Sets the fragment result and sends the user to the user profile.
        getParentFragmentManager().setFragmentResult(UserProfileFragment.REQUEST, result);
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_details_to_navigation_user_profile);
    }

    /**
     * Loads the ingredient list from given recipe's id.
     * @param recipeId - the recipe's id.
     */
    private void loadIngredientList(String recipeId) {
        detailsViewModel.loadIngredientList(recipeId).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                detailsViewModel.setIngredientList(((Result.Success<List<Ingredient>>) result).getData());
            } else {
                Snackbar.make(binding.getRoot(), R.string.ingredient_list_load_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> loadStepList(recipeId))
                        .show();
            }
        });
    }

    /**
     * Loads the step list from given recipe's id.
     * @param recipeId - the recipe's id.
     */
    private void loadStepList(String recipeId) {
        detailsViewModel.loadStepsList(recipeId).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                detailsViewModel.setStepList(((Result.Success<List<Step>>) result).getData());
            } else {
                Snackbar.make(binding.getRoot(), R.string.step_list_load_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> loadStepList(recipeId))
                        .show();
            }
        });
    }

    /**
     * Set ups the recycler views from the fragment and the adapters.
     */
    private void setupRecyclerViews() {
        ingredientsAdapter = new RecipeDetailsIngredientAdapter();
        stepsAdapter = new RecipeDetailsStepAdapter();

        binding.rvIngredientList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIngredientList.setAdapter(ingredientsAdapter);

        binding.rvStepList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStepList.setAdapter(stepsAdapter);
    }

}