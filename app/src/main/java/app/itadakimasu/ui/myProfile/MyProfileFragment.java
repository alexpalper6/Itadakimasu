package app.itadakimasu.ui.myProfile;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.databinding.FragmentMyProfileBinding;
import app.itadakimasu.ui.adapters.ProfileRecipesAdapter;
import app.itadakimasu.ui.recipeCreation.RecipeCreationFragment;
import app.itadakimasu.ui.recipeDetails.RecipeDetailsFragment;

/**
 * Fragment that shows the authenticated user's recipes and a log out option.
 */
@SuppressWarnings("unchecked")
public class MyProfileFragment extends Fragment {

    // My profile binding layout.
    private FragmentMyProfileBinding binding;
    // Profile's recipes adapter for the recycler view.
    private ProfileRecipesAdapter adapter;
    // View model that holds this fragment's data.
    private MyProfileViewModel myProfileViewModel;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // View model instantiation and setting username and photoUrl
        myProfileViewModel = new ViewModelProvider(this).get(MyProfileViewModel.class);
        // Sets the user's name and photo with the authenticated user's data.
        myProfileViewModel.setProfileUsername(myProfileViewModel.getAuthUsername());
        myProfileViewModel.setPhotoUrl(myProfileViewModel.getAuthUserPhotoUrl());
        binding.tvUsername.setText(myProfileViewModel.getProfileUsername());
        binding.srlRefresh.setColorSchemeResources(R.color.primaryColor);
        //Initiates the recycler view and adapter.
        initiateRvLayout();

        setAuthUserImage();

        // Load the user's first recipes if the list is empty.
        if (myProfileViewModel.isListEmpty()) {
            loadFirstRecipes();
        }

        // Observables and Listeners //

        myProfileViewModel.getRecipeList().observe(getViewLifecycleOwner(), list -> adapter.submitList(new ArrayList<>(list)));

        // Load the first recipes when the user refresh the list pulling down on the recycler view
        // thanks to SwipeRefreshLayout refresh listener.
        binding.srlRefresh.setOnRefreshListener(this::loadFirstRecipes);

        // Implementation of OnClickDisplayListener interface, sends the recipe data to the Recipe Details
        // fragment.
        adapter.setOnClickDisplayListener(this::showDetailsRecipe);

        adapter.setOnClickRemoveListener(this::showDelDialogOfRecipe);

        adapter.setOnClickEditListener(this::editRecipe);

        // Listener of recycler view, used for pagination, charging the next recipes when the recycler view
        // reaches its end.
        binding.rvRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // With loadingData we assure that when is loading the user wont be able to call for data again
                if ((!recyclerView.canScrollVertically(1)) && !myProfileViewModel.isLoadingData() && !myProfileViewModel.reachedEndPagination()) {
                    if (!myProfileViewModel.isListEmpty()) {
                        loadNextRecipes();
                    }
                }
            }
        });

        // Signs out the user when the sign out image button is tapped.
        binding.ibSignOut.setOnClickListener(v -> myProfileViewModel.signOut());
        // Goes to the fragment to edit the user's profile data.
        binding.ibEdit.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_my_profile_to_navigation_edit_profile));
    }

    /**
     * Downloads the user's image and loads it.
     */
    private void setAuthUserImage() {
        myProfileViewModel.downloadAuthUserImageUri().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                Uri imageUri = ((Result.Success<Uri>) result).getData();
                loadUserImage(imageUri);
            } else {
                Snackbar.make(binding.getRoot(), R.string.image_load_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> setAuthUserImage())
                        .show();
            }
        });
    }

    /**
     * Sends the user to recipe creation fragment with the actual data to edit it.
     * @param recipePosition - the position from the selected recipe on the list.
     */
    private void editRecipe(int recipePosition) {
        Bundle result = new Bundle();
        result.putParcelable(RecipeCreationFragment.RESULT, myProfileViewModel.getRecipeFromList(recipePosition));
        getParentFragmentManager().setFragmentResult(RecipeCreationFragment.REQUEST, result);
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_profile_to_creation_navigation);
    }

    /**
     * Shows confirm dialog to delete recipe.
     * @param recipePosition - the position from the selected recipe on the list.
     */
    private void showDelDialogOfRecipe(int recipePosition) {
        Recipe recipeToDelete = myProfileViewModel.getRecipeFromList(recipePosition);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.delete_recipe)
                .setMessage(getString(R.string.delete_recipe_desc, recipeToDelete.getTitle()))
                .setPositiveButton(R.string.delete, (dialog, which) -> removeRecipeFromFavourites(recipeToDelete, recipePosition))
                .setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.cancel()));

        builder.show();
    }

    /**
     * Sends the user to recipe details fragment with the selected recipe's data.
     * @param recipePosition - the position from the selected recipe on the list.
     */
    private void showDetailsRecipe(int recipePosition) {
        Bundle result = new Bundle();
        result.putParcelable(RecipeDetailsFragment.RESULT, myProfileViewModel.getRecipeFromList(recipePosition));
        getParentFragmentManager().setFragmentResult(RecipeDetailsFragment.REQUEST, result);
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_profile_to_navigation_details);
    }


    /**
     * Removes every entry that contains the recipe on favourites collection.
     * After that, if the result is successful, the recipe will be removed from the recipes collection.
     * @param recipeToDelete - recipe to remove.
     * @param itemPosition - the position from the list, to remove it later from it.
     */
    private void removeRecipeFromFavourites(Recipe recipeToDelete, int itemPosition) {
        setDataIsLoading();

        myProfileViewModel.removeAllFavouritesWithRecipe(recipeToDelete.getId()).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                removeRecipe(recipeToDelete, itemPosition);
            } else {
                setDataIsRetrieved();
                Snackbar.make(binding.getRoot(), R.string.recipe_delete_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> removeRecipeFromFavourites(recipeToDelete, itemPosition))
                        .show();
            }
        });


    }

    /**
     * Removes the recipe from the recipes collection.
     * If successful, the image reference on firebase storage will be removed.
     * @param recipeToDelete - the recipe's data to be removed.
     * @param itemPosition - the position where the recipe will be removed.
     */
    private void removeRecipe(Recipe recipeToDelete, int itemPosition) {
        myProfileViewModel.deleteRecipe(recipeToDelete.getId()).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                myProfileViewModel.removeRecipeAt(itemPosition);
                deleteRecipeImage(recipeToDelete.getPhotoUrl());
            } else {
                Snackbar.make(binding.getRoot(), R.string.recipe_delete_error, Snackbar.LENGTH_LONG).show();
                setDataIsRetrieved();
            }
        });
    }

    /**
     * Finally, the recipe's image will be removed.
     */
    private void deleteRecipeImage(String recipePhotoUrl) {

        myProfileViewModel.deleteRecipeImage(recipePhotoUrl).observe(getViewLifecycleOwner(), result -> {
            setDataIsRetrieved();
            if (result instanceof Result.Error) {
                Snackbar.make(binding.getRoot(), R.string.recipe_delete_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Loads the user's image getting the Image Reference.
     * Using the bitmap of the image, android's palette library will be used to obtain the muted
     * color and it will establish it to the profile background.
     */
    private void loadUserImage(Uri uriImage) {
        Glide.with(requireContext()).asBitmap().load(uriImage).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_default_user_profile)
                .error(R.drawable.ic_default_user_profile)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                            Palette p = Palette.from(resource).generate();
                            binding.getRoot().setBackgroundColor(p.getMutedColor(ContextCompat.getColor(requireContext(), R.color.primaryColor)));
                        }
                        return false;
                    }
                }).into(binding.ivUserImage);
    }

    /**
     * Load the first recipes of the user, this method is used when the recycler view is reloaded and
     * when it loads the fragment for first time.
     */
    private void loadFirstRecipes() {
        setDataIsLoading();

        myProfileViewModel.loadFirstRecipes().observe(getViewLifecycleOwner(), result -> {
            setDataIsRetrieved();
            binding.srlRefresh.setRefreshing(false);

            if (result instanceof Result.Success) {
                List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                myProfileViewModel.setPaginationEndState(recipeList.size() < RecipesRepository.LIMIT_QUERY);
                myProfileViewModel.setRecipesList(recipeList);

            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Loads the next recipes paginating.
     * We get recipes by the limit query implemented in RecipesRepository,
     * if the size of list cannot be divided by that limit having 0 as a remainder,
     * ie: that the list should be multiples of 10 (20, 30, 40, 50, 60...),
     * then the system wont call for more recipes, because the list of recipes reached to the end.
     */
    private void loadNextRecipes() {
        setDataIsLoading();

        myProfileViewModel.loadNextRecipes().observe(getViewLifecycleOwner(), result -> {
            setDataIsRetrieved();

            if (result instanceof Result.Success) {
                List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                myProfileViewModel.setPaginationEndState(recipeList.size() < RecipesRepository.LIMIT_QUERY);
                myProfileViewModel.addRetrievedRecipes(recipeList);
            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }


    /**
     * Initiates the adapter and recycler layout manager.
     */
    private void initiateRvLayout() {
        adapter = new ProfileRecipesAdapter(requireContext(), getViewLifecycleOwner());
        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipes.setAdapter(adapter);
    }

    /**
     * Sets the progress bar visible and the loading data state as true, stating that
     * there is data loading.
     */
    private void setDataIsLoading() {
        binding.pbProgress.setVisibility(View.VISIBLE);
        myProfileViewModel.setLoadingDataState(true);
    }

    /**
     * Sets the progress var invisible and data state as false, stating that the data finished
     * to be loaded.
     */
    private void setDataIsRetrieved() {
        binding.pbProgress.setVisibility(View.GONE);
        myProfileViewModel.setLoadingDataState(false);
    }

    /**
     * Sets binding to null when the fragment is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}