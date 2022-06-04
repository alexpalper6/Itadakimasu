package app.itadakimasu.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.databinding.FragmentHomeBinding;
import app.itadakimasu.ui.adapters.RecipePreviewAdapter;
import app.itadakimasu.ui.recipeDetails.RecipeDetailsFragment;
import app.itadakimasu.ui.userProfile.UserProfileFragment;

/**
 * Main section fragment of the app, loads the newest recipes from every user.
 * It also has a search view that lets filter by title.
 */
@SuppressWarnings("unchecked")
public class HomeFragment extends Fragment {
    // Binding layout of home fragment.
    private FragmentHomeBinding binding;
    // View Model for home fragment.
    private HomeViewModel homeViewModel;
    // Recipe preview adapter instance for home recycler view.
    private RecipePreviewAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // Sets the recycler view and the adapter.
        setAdapterRecyclerView();
        // Changes the color of the refresh layout icon.
        binding.srlRefresh.setColorSchemeResources(R.color.primaryColor);

        // When the list is empty (the view model is created along with the fragment) then it fetches
        // the newest recipes.
        if (homeViewModel.isListEmpty()) {
            loadFirstRecipes();
        }

        // Observable that submit the list on the adapter when the list changes.
        homeViewModel.getRecipeList().observe(getViewLifecycleOwner(), recipeList -> adapter.submitList(new ArrayList<>(recipeList)));

        // Shows the user profile from the author of selected recipe.
        adapter.setOnClickShowProfileListener(this::showUserProfileOfRecipe);
        // Shows the user the details of the selected recipe.
        adapter.setOnClickDisplayListener(this::displayRecipeDetails);
        // Add a selected recipes to favourites.
        adapter.setOnClickAddFavListener(this::addRecipeToFavourites);

        // Listener for the refresh layout, when refreshed, it fetches the newest recipes.
        binding.srlRefresh.setOnRefreshListener(this::loadFirstRecipes);
        // Scroll listener to paginate data.
        binding.rvRecipeList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // When the users reaches the end of the recycler view and there is no data loading and the user can paginate data
                // then it will load the next favourites if the list is not empty.
                if ((!recyclerView.canScrollVertically(1)) && !homeViewModel.isLoadingData() && !homeViewModel.reachedEndPagination()) {
                    if (!homeViewModel.isListEmpty()) {
                        loadNextRecipes();
                    }

                }
            }
        });
    }

    /**
     * Sends the user to the details fragment with the recipe's data to be displayed.
     * @param recipePosition - the selected recipe's position on the list.
     */
    private void displayRecipeDetails(int recipePosition) {
        Bundle result = new Bundle();
        result.putParcelable(RecipeDetailsFragment.RESULT, homeViewModel.getRecipeAt(recipePosition));
        getParentFragmentManager().setFragmentResult(RecipeDetailsFragment.REQUEST, result);
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_navigation_details);
    }

    /**
     * Sends the user another user's profile, the user's data is retrieved from the selected recipe.
     * @param recipePosition - the selected recipe's position on the list.
     */
    private void showUserProfileOfRecipe(int recipePosition) {
        Bundle result = new Bundle();
        // Obtains the recipe's data in order to get the author name and photo.
        Recipe userRecipe = homeViewModel.getRecipeAt(recipePosition);
        result.putString(UserProfileFragment.RESULT_USERNAME, userRecipe.getAuthor());
        result.putString(UserProfileFragment.RESULT_USER_PHOTO, userRecipe.getPhotoAuthorUrl());

        getParentFragmentManager().setFragmentResult(UserProfileFragment.REQUEST, result);
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_navigation_user_profile);
    }

    /**
     * Adds a recipe to favourites collection as an document, if the upload is successful, then
     * the recipe's checkbox will be checked.
     * @param recipePosition - the selected recipe's position on the list.
     */
    private void addRecipeToFavourites(int recipePosition) {
        // If the author is the same as the authenticated user, it won't be added to favourites.
        Recipe recipeToFav = homeViewModel.getRecipeAt(recipePosition);
        if (!recipeToFav.getAuthor().equals(homeViewModel.getAuthUsername())) {
            setDataIsLoading();
            homeViewModel.addRemoveFavourite(recipeToFav).observe(getViewLifecycleOwner(), result -> {
                setDataIsRetrieved();
                if (result instanceof Result.Success) {
                    homeViewModel.markRecipeFavouriteAt(recipePosition);
                    // Notifies the adapter that the item state changed.
                    adapter.notifyItemChanged(recipePosition);
                } else {
                    Snackbar.make(binding.getRoot(), R.string.add_fav_error, Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Loads the first newest recipes on the list, when the list is fetched, then every recipe inside
     * the list will be checked if its a favourite one from the user.
     */
    private void loadFirstRecipes() {
        // Clears the recipe first, so if there is data, it's cleared before fetching the new one.
        homeViewModel.clearRecipeList();
        setDataIsLoading();

        homeViewModel.loadFirstRecipes().observe(getViewLifecycleOwner(), result -> {
            setDataIsRetrieved();
            binding.srlRefresh.setRefreshing(false);

            if (result instanceof Result.Success) {
                List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                homeViewModel.setReachedEndPaginationState(recipeList.size() < RecipesRepository.LIMIT_QUERY);
                checkForFavouriteRecipes(recipeList);

            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Loads for the next recipes when paginating.
     * Sets new state for end pagination, true if the retrieved list's size is not as big as the quantity of entries
     * it should fetch.
     */
    private void loadNextRecipes() {
        setDataIsLoading();
        homeViewModel.loadNextRecipes().observe(getViewLifecycleOwner(), result -> {
            setDataIsRetrieved();

            if (result instanceof Result.Success) {
                List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                homeViewModel.setReachedEndPaginationState(recipeList.size() < RecipesRepository.LIMIT_QUERY);
                checkForFavouriteRecipes(recipeList);
            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This method overrides the value of the list with this recipes after being checked if the recipes are checked as favourite.
     *
     * Iterates the list of recipes, and for each one checks if the authenticated user added it as favourite,
     * in case the recipe is from the same user, it will just continue.
     *
     * This uses an synchronous method from the view model, is an integer flag that sets the quantity of
     * recipes to process, when the number reach to 0, then the list is added.
     * @param recipeList - list of recipes.
     */
    private void checkForFavouriteRecipes(List<Recipe> recipeList) {
        // Sets the quantity of recipes to process
        homeViewModel.setRecipesToProcess(recipeList.size());
        for (Recipe recipe : recipeList) {
            // If the recipe is from the same user as the authenticated, then it will just mark it as processed.
            if (!recipe.getAuthor().equals(homeViewModel.getAuthUsername())) {
                homeViewModel.isRecipeFavourite(recipe).observe(getViewLifecycleOwner(), favouriteResult -> {
                    // If the repository returns a successful value sets true if it has found the entry
                    // on favourites; false if not.
                    if (favouriteResult instanceof Result.Success) {
                        recipe.setFavourite(((Result.Success<Boolean>) favouriteResult).getData());
                    }
                    // Its necessary to do this twice because the asynchronous methods could spend
                    // more time than expected.
                    if (homeViewModel.recipeIsProcessed() == 0) {
                        homeViewModel.addRetrievedRecipes(recipeList);
                    }

                });
            } else {
                if (homeViewModel.recipeIsProcessed() == 0) {
                    homeViewModel.addRetrievedRecipes(recipeList);
                }
            }
        }
    }

    /**
     * Sets the loading data state as true, so the user won't be able to fetch more data while it's
     * getting data already from the database, and shows the progress bar as visible in order to let
     * the user watch the progress.
     */
    private void setDataIsLoading() {
        homeViewModel.setLoadingDataState(true);
        binding.pbProgress.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the loading data state as false, the data is fetched so the user will be able to get more
     * data, and the progress bar visibility will be hid.
     */
    private void setDataIsRetrieved() {
        homeViewModel.setLoadingDataState(false);
        binding.pbProgress.setVisibility(View.GONE);
    }

    /**
     * Sets the recycler view and adapter.
     */
    private void setAdapterRecyclerView() {
        adapter = new RecipePreviewAdapter(requireContext(), getViewLifecycleOwner());
        binding.rvRecipeList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipeList.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}