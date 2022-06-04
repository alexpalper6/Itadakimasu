package app.itadakimasu.ui.userProfile;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import app.itadakimasu.databinding.FragmentUserProfileBinding;
import app.itadakimasu.ui.adapters.ProfileRecipesAdapter;
import app.itadakimasu.ui.recipeDetails.RecipeDetailsFragment;

/**
 * Fragment used for watching other user's profiles.
 */
@SuppressWarnings("unchecked")
public class UserProfileFragment extends Fragment {
    // Constant used to obtain the author's data from a recipe on other fragments as a result fragment.
    public static final String REQUEST = "app.itadakimasu.ui.userProfile.Request";
    public static final String RESULT_USERNAME = "app.itadakimasu.ui.userProfile.ResultUsername";
    public static final String RESULT_USER_PHOTO = "app.itadakimasu.ui.userProfile.ResultUserPhoto";

    // The fragment's binding layout.
    private FragmentUserProfileBinding binding;
    // Adapter for the list of recipes.
    private ProfileRecipesAdapter adapter;
    // View model that holds the data and survives configuration changes.
    private UserProfileViewModel userProfileViewModel;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        // Initiates the adapter and recycler view
        initiateRvLayout();
        binding.srlRefresh.setColorSchemeResources(R.color.primaryColor);

        // Obtains the user from which recipes will be fetched with.
        getParentFragmentManager().setFragmentResultListener(UserProfileFragment.REQUEST, this, (requestKey, result) -> {
            String username = result.getString(RESULT_USERNAME);
            String userPhotoUrl = result.getString(RESULT_USER_PHOTO);

            userProfileViewModel.setProfileUsername(username);
            userProfileViewModel.setPhotoUrl(userPhotoUrl);
        });

        // When the view model receives the username obtained from the fragment result, load the first
        // recipes from that user.
        userProfileViewModel.getProfileUsername().observe(getViewLifecycleOwner(), username -> {
            binding.tvUsername.setText(username);
            if (userProfileViewModel.isListEmpty()) {
                loadFirstRecipes();
            }
        });

        // When the view model receives the username obtained from the fragment result, load user's image.
        userProfileViewModel.getPhotoUrl().observe(getViewLifecycleOwner(), this::setUserImage);

        userProfileViewModel.getRecipeList().observe(getViewLifecycleOwner(), list -> adapter.submitList(new ArrayList<>(list)));

        // Sends the recipe data to the Recipe Details fragment in order to see it.
        adapter.setOnClickDisplayListener(this::showRecipeDetails);

        binding.ibGoBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Load the first recipes when the user refresh the list by pulling down.
        binding.srlRefresh.setOnRefreshListener(this::loadFirstRecipes);

        // Listener of recycler view, used for pagination, this charges the next recipes when the recycler view
        // reaches its end.
        binding.rvRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // With loadingData we assure that when is loading the user wont be able to call for data again.
                if ((!recyclerView.canScrollVertically(1)) && !userProfileViewModel.isLoadingData() && !userProfileViewModel.reachedEndPagination()) {
                    if (!userProfileViewModel.isListEmpty()) {
                        loadNextRecipes();
                    }
                }
            }
        });


    }

    /**
     * Downloads the user's image and loads it into the UI.
     * @param userImageUrl - the user's image url where the data is stored.
     */
    private void setUserImage(String userImageUrl) {
        userProfileViewModel.downloadUserImage(userImageUrl).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                Uri imageUri = ((Result.Success<Uri>) result).getData();
                loadUserImage(imageUri);
            } else {
                Snackbar.make(binding.getRoot(), R.string.image_load_error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> setUserImage(userImageUrl))
                        .show();
            }
        });
    }

    /**
     * Shows the selected recipe's details.
     * @param itemPosition - the selected recipe's position on the list.
     */
    private void showRecipeDetails(int itemPosition) {
        Bundle result = new Bundle();
        result.putParcelable(RecipeDetailsFragment.RESULT, userProfileViewModel.getRecipeFromList(itemPosition));
        getParentFragmentManager().setFragmentResult(RecipeDetailsFragment.REQUEST, result);
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_user_profile_to_navigation_details);
    }

    /**
     * Loads the first recipes from one user.
     * If the load fails, an error will prompt to the user.
     */
    private void loadFirstRecipes() {
        setDataIsLoading();

        userProfileViewModel.loadFirstRecipes().observe(getViewLifecycleOwner(), result -> {
            setDataIsRetrieved();
            binding.srlRefresh.setRefreshing(false);

            if (result instanceof Result.Success) {
                List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                userProfileViewModel.setRecipesList(recipeList);
                userProfileViewModel.setPaginationEndState(recipeList.size() < RecipesRepository.LIMIT_QUERY);
            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Loads the next recipes with pagination.
     * We get recipes by the limit query implemented in RecipesRepository,
     * if the size of list cannot be divided by that limit having 0 as a remainder,
     * ie: that the list should be multiples of 10 (20, 30, 40, 50, 60...),
     * then the system wont call for more recipes, because the list of recipes reached to the end.
     */
    private void loadNextRecipes() {
        setDataIsLoading();

        userProfileViewModel.loadNextRecipes().observe(getViewLifecycleOwner(), result -> {
            setDataIsRetrieved();

            if (result instanceof Result.Success) {
                List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                userProfileViewModel.setPaginationEndState(recipeList.size() < RecipesRepository.LIMIT_QUERY);
                userProfileViewModel.addRetrievedRecipes(recipeList);
            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, Snackbar.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Shows the progress bar and sets the loading data as true.
     */
    private void setDataIsLoading() {
        binding.pbProgress.setVisibility(View.VISIBLE);
        userProfileViewModel.setLoadingDataState(true);
    }

    /**
     * Hides the progress bar and sets the loading data as false.
     */
    private void setDataIsRetrieved() {
        binding.pbProgress.setVisibility(View.GONE);
        userProfileViewModel.setLoadingDataState(false);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}