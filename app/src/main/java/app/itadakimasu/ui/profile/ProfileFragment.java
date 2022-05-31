package app.itadakimasu.ui.profile;

import android.app.AlertDialog;
import android.graphics.Bitmap;
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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.databinding.FragmentProfileBinding;
import app.itadakimasu.ui.recipeCreation.RecipeCreationFragment;
import app.itadakimasu.ui.recipeDetails.RecipeDetailsFragment;

// TODO: Add the functionality to retrieve the profile of another user when tapping on their image
//      on the recipe. For this, use a fragmentResultManager!
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileRecipesAdapter adapter;
    private ProfileViewModel profileViewModel;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel notificationsViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // View model instantiation and setting username and photoUrl
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // TODO: Remember to change this, the username and photo url will be retrieved
        //      via fregmentResultListener when the user image is tapped on a recipe.
        //      Keep in mind that these 2 lines will have to be keeped in order to retrieve the user data
        //      when the authenticated user goest to their profile using the bottom navigation menu.
        profileViewModel.setProfileUsername(profileViewModel.getAuthUsername());
        profileViewModel.setPhotoUrl(profileViewModel.getAuthUserPhotoUrl());

        binding.tvUsername.setText(profileViewModel.getProfileUsername());
        binding.srlRefresh.setColorSchemeResources(R.color.primaryColor);

        initiateRvLayout();
        loadUserImage();

        if (profileViewModel.isListEmpty()) {
            binding.pbProgress.setVisibility(View.VISIBLE);
            loadFirstRecipes();
        }

        // Observables and Listeners //

        profileViewModel.getRecipeList().observe(getViewLifecycleOwner(), list -> adapter.submitList(new ArrayList<>(list)));

        // Load the first recipes when the user refresh the list pulling down on the recycler view
        // thanks to SwipeRefreshLayout refresh listener.
        binding.srlRefresh.setOnRefreshListener(this::loadFirstRecipes);

        // Implementation of OnClickDisplayListener interface, sends the recipe data to the Recipe Details
        // fragment.
        adapter.setOnClickDisplayListener(itemPosition -> {
            Bundle result = new Bundle();
            result.putParcelable(RecipeDetailsFragment.RESULT, profileViewModel.getRecipeFromList(itemPosition));
            getParentFragmentManager().setFragmentResult(RecipeDetailsFragment.REQUEST, result);
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_profile_to_navigation_details);
        });

        adapter.setOnClickRemoveListener(itemPosition -> {
            Recipe recipeToDelete = profileViewModel.getRecipeFromList(itemPosition);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.delete_recipe)
                    .setMessage(getString(R.string.delete_recipe_desc, recipeToDelete.getTitle()))
                    .setPositiveButton(R.string.delete, (dialog, which) -> removeRecipe(recipeToDelete, itemPosition))
                    .setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.cancel()));

            builder.show();
        });

        adapter.setOnClickEditListener(itemPosition -> {
            Bundle result = new Bundle();
            result.putParcelable(RecipeCreationFragment.RESULT, profileViewModel.getRecipeFromList(itemPosition));
            getParentFragmentManager().setFragmentResult(RecipeCreationFragment.REQUEST, result);
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_profile_to_creation_navigation);
        });

        // Listener of recycler view, used for pagination, charging the next recipes when the recycler view
        // reaches its end.
        binding.rvRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // With loadingData we assure that when is loading the user wont be able to call for data again
                if ((!recyclerView.canScrollVertically(1)) && !profileViewModel.isLoadingData() && !profileViewModel.reachedEndPagination()) {
                    profileViewModel.setLoadingDataState(true);
                    loadNextRecipes();
                }
            }
        });

        // Signs out the user when the sign out image button is tapped.
        binding.ibSignOut.setOnClickListener(v -> profileViewModel.signOut());


    }

    /**
     * First, the recipe's image will be removed.
     */
    private void deleteRecipeImage(String recipePhotoUrl) {

        profileViewModel.deleteRecipeImage(recipePhotoUrl).observe(getViewLifecycleOwner(), result -> {
            binding.pbProgress.setVisibility(View.GONE);
            profileViewModel.setLoadingDataState(false);
            if (result instanceof Result.Error) {
                Snackbar.make(binding.getRoot(), R.string.recipe_delete_error, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

    //TODO: Remove every document from favourites collection that matches the recipeId
    private void removeRecipe(Recipe recipeToDelete, int itemPosition) {
        binding.pbProgress.setVisibility(View.VISIBLE);
        profileViewModel.setLoadingDataState(true);

        profileViewModel.deleteRecipe(recipeToDelete.getId()).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                profileViewModel.removeRecipeAt(itemPosition);
                deleteRecipeImage(recipeToDelete.getPhotoUrl());
            } else {
                Snackbar.make(binding.getRoot(), R.string.recipe_delete_error, BaseTransientBottomBar.LENGTH_LONG).show();
                binding.pbProgress.setVisibility(View.GONE);
                profileViewModel.setLoadingDataState(false);
            }
        });
    }


    /**
     * Loads the user's image getting the Image Reference.
     * Using the bitmap of the image, android's palette library will be used to obtain the muted
     * color and it will establish it to the profile background.
     */
    private void loadUserImage() {
        StorageReference reference = profileViewModel.getUserImageReference();
        Glide.with(requireContext()).asBitmap().load(reference).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_default_user_profile)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                            Palette p = Palette.from(resource).generate();
                            // https://www.section.io/engineering-education/extracting-colors-from-image-using-palette-api-in-android/
                            binding.getRoot().setBackgroundColor(p.getMutedColor(ContextCompat.getColor(requireContext(), R.color.primaryColor)));
                            //binding.ibSignOut.setColorFilter(p.getDominantSwatch().getBodyTextColor());
                            //binding.ibEdit.setColorFilter(p.getDominantSwatch().getBodyTextColor());
                            //binding.tvUsername.setTextColor(p.getDominantSwatch().getBodyTextColor());
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
        profileViewModel.loadFirstRecipes().observe(getViewLifecycleOwner(), result -> {
            binding.pbProgress.setVisibility(View.GONE);
            binding.srlRefresh.setRefreshing(false);
            profileViewModel.setLoadingDataState(false);
            profileViewModel.setPaginationEndState(false);

            if (result instanceof Result.Success) {
                profileViewModel.setRecipesList(((Result.Success<List<Recipe>>) result).getData());

            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, BaseTransientBottomBar.LENGTH_LONG).show();
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
        if (profileViewModel.getListSize() % RecipesRepository.LIMIT_QUERY == 0) {
            binding.pbProgress.setVisibility(View.VISIBLE);
            profileViewModel.loadNextRecipes().observe(getViewLifecycleOwner(), result -> {
                binding.pbProgress.setVisibility(View.GONE);
                profileViewModel.setLoadingDataState(false);

                if (result instanceof Result.Success) {
                    List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                    profileViewModel.setPaginationEndState(recipeList.isEmpty());
                    profileViewModel.addRetrievedRecipes(recipeList);
                } else {
                    Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, BaseTransientBottomBar.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Initiates the adapter and recycler layout manager.
     */
    private void initiateRvLayout() {
        adapter = new ProfileRecipesAdapter(requireContext());
        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipes.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}