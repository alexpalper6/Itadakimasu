package app.itadakimasu.ui.favourites;

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


import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Favourite;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.FavouritesRepository;
import app.itadakimasu.databinding.FragmentFavouritesBinding;
import app.itadakimasu.ui.adapters.RecipePreviewAdapter;
import app.itadakimasu.ui.recipeDetails.RecipeDetailsFragment;
import app.itadakimasu.ui.userProfile.UserProfileFragment;

/**
 * Fragment where the authenticated user can search through the recipes that they added to favourites.
 */
@SuppressWarnings("unchecked")
public class FavouritesFragment extends Fragment {
    // Binding layout of favourites fragment.
    private FragmentFavouritesBinding binding;
    // Adapter for favourites recycler view, in order to show the recipes.
    private RecipePreviewAdapter adapter;
    // View model that holds the elements showed to the user.
    private FavouritesViewModel favouritesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFavouritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        favouritesViewModel = new ViewModelProvider(this).get(FavouritesViewModel.class);
        // Sets the adapter and the recycler view.
        setAdapterRecyclerView();
        binding.srlRefresh.setColorSchemeResources(R.color.primaryColor);

        // If the list is empty, the first favourite recipes will be loaded.
        if (favouritesViewModel.isListEmpty()) {
            loadNewestFavourites();
        }

        // Observers and listeners //

        // Observes for changes on the list of recipes and updates the list on the adapter.
        favouritesViewModel.getRecipeList().observe(getViewLifecycleOwner(), recipeList -> adapter.submitList(new ArrayList<>(recipeList)));
        // Sends the user to the user's profile of the selected recipe.
        adapter.setOnClickShowProfileListener(this::showUserProfileOfRecipe);
        // Sends the user to the selected recipe's details.
        adapter.setOnClickDisplayListener(this::displayRecipeDetails);
        // Removes the selected recipe from favourites.
        adapter.setOnClickAddFavListener(this::removeFromFavourites);



        // Listener for the refresh layout, when refreshed, it fetches the newest recipes.
        binding.srlRefresh.setOnRefreshListener(this::loadNewestFavourites);
        // Scroll listener that enables pagination on the fragment.
        binding.rvRecipeList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // If the user reached to the end of the list, there is no data loading and the user can paginate data
                // then it'll load the next favourites if the list is not empty.
                if ((!recyclerView.canScrollVertically(1)) && !favouritesViewModel.isLoadingData() && !favouritesViewModel.reachedEndPagination()) {
                    if (!favouritesViewModel.isListEmpty()) {
                        loadNextFavourites();
                    }
                }
            }
        });
    }

    /**
     * Removes the favourite entry on the collection and removes the recipe from the list when it's done.
     * @param recipePosition - the selected recipe's position on the list.
     */
    private void removeFromFavourites(int recipePosition) {
        setDataIsLoading();
        favouritesViewModel.removeFavourite(recipePosition).observe(getViewLifecycleOwner(), result -> {
            setDataIsRetrieved();
            if (result instanceof Result.Success) {
                favouritesViewModel.removeRecipeAt(recipePosition);
            } else {
                Snackbar.make(binding.getRoot(), R.string.remove_fav_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Obtains the recipe's data from the list and sends the user to details fragment with the recipe's data.
     * @param recipePosition - the selected recipe's position on the list.
     */
    private void displayRecipeDetails(int recipePosition) {
        Bundle result = new Bundle();
        result.putParcelable(RecipeDetailsFragment.RESULT, favouritesViewModel.getRecipeAt(recipePosition));
        getParentFragmentManager().setFragmentResult(RecipeDetailsFragment.REQUEST, result);
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_favourites_to_navigation_details);
    }

    /**
     * Shows the user the profile of the user who created the recipe.
     * @param recipePosition - the selected recipe's position on the list.
     */
    private void showUserProfileOfRecipe(int recipePosition) {
        Bundle result = new Bundle();
        // Obtains the user's data from the selected recipe.
        Recipe userRecipe = favouritesViewModel.getRecipeAt(recipePosition);
        result.putString(UserProfileFragment.RESULT_USERNAME, userRecipe.getAuthor());
        result.putString(UserProfileFragment.RESULT_USER_PHOTO, userRecipe.getPhotoAuthorUrl());

        getParentFragmentManager().setFragmentResult(UserProfileFragment.REQUEST, result);
        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_favourites_to_navigation_user_profile);
    }

    /**
     * Loads the first newest entries that the authenticated user added to the favourites collection.
     * The data will stop loading when it fails of after loading the recipe's data.
     */
    private void loadNewestFavourites() {
        // Clears the recipe list.
        favouritesViewModel.clearRecipeList();
        setDataIsLoading();

        // Observes for the data returned by the repository, if is successful, loads the recipe data;
        // if it fails, then an error message will be showed to the user.
        favouritesViewModel.loadNewestFavourites().observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                List<Favourite> favouriteList = ((Result.Success<List<Favourite>>) result).getData();
                // If the list's size is less than the quantity of documents that should have retrieved,
                // then there wont be more documents to retrieve, so the user won't be able to paginate.
                favouritesViewModel.setReachedEndPagination(favouriteList.size() < FavouritesRepository.LIMIT_QUERY);

                // If the list is not empty, sets the las fav date that will be used to paginate
                // and loads the recipes' data.
                if (!favouriteList.isEmpty()) {
                    favouritesViewModel.setLastFavDate(favouriteList.get(favouriteList.size() - 1).getAdditionDate());
                    loadRecipesData(favouriteList);
                } else {
                    setDataIsRetrieved();
                }
            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, Snackbar.LENGTH_LONG).show();
                setDataIsRetrieved();
            }
        });
    }

    /**
     * Loads for the next favourites entries, used for pagination.
     * The data will stop loading when it fails of after loading the recipe's data.
     */
    private void loadNextFavourites() {
        setDataIsLoading();
        favouritesViewModel.loadNextFavourites().observe(getViewLifecycleOwner(), result -> {

            if (result instanceof Result.Success) {
                // Obtains the favourite entries.
                List<Favourite> favouriteList = ((Result.Success<List<Favourite>>) result).getData();
                // The user won't be able to paginate if the list is smaller than the quantity of favourites
                // entries the repository is supposed to send.
                favouritesViewModel.setReachedEndPagination(favouriteList.size() < FavouritesRepository.LIMIT_QUERY);
                // Sets the last favourite entry date for pagination and loads the recipes data.
                if (!favouriteList.isEmpty()) {
                    favouritesViewModel.setLastFavDate(favouriteList.get(favouriteList.size() - 1).getAdditionDate());
                    loadRecipesData(favouriteList);
                } else {
                    setDataIsRetrieved();
                }
            } else {
                setDataIsRetrieved();
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, Snackbar.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Using the list of favourites entries, load their respective recipe data with the recipeId
     * that each entry has.
     * @param favouriteList - the list of favourite entries.
     */
    private void loadRecipesData(List<Favourite> favouriteList) {
        // Sets the quantity of recipes that must be processed to set add the entries to the recipe list.
        favouritesViewModel.setRecipesToProcess(favouriteList.size());
        List<Recipe> recipeListToAdd = new ArrayList<>();
        // For each entry, its recipe's data will be loaded and settled on each respective position.
        for (int i = 0; i < favouriteList.size(); i++) {
            // Creates an empty place on the list, the recipe will be settled there when the data is retrieved.
            recipeListToAdd.add(null);
            int position = i;

            String recipeIdToLoad = favouriteList.get(position).getRecipeId();
            favouritesViewModel.loadRecipeData(recipeIdToLoad).observe(getViewLifecycleOwner(), result -> {
                if (result instanceof Result.Success) {
                    Recipe recipe = ((Result.Success<Recipe>) result).getData();
                    recipe.setFavourite(true);
                    synchronized (this) {
                        recipeListToAdd.set(position, recipe);
                    }
                }

                if (favouritesViewModel.recipeProcessed() == 0) {
                    setDataIsRetrieved();
                    binding.srlRefresh.setRefreshing(false);

                    // Removes the null fields on the list.
                    recipeListToAdd.removeAll(Collections.singleton(null));
                    favouritesViewModel.addRecipes(recipeListToAdd);
                }
            });
        }
    }

    /**
     * Settles the adapter and recycler view.
     */
    private void setAdapterRecyclerView() {
        adapter = new RecipePreviewAdapter(requireContext(), getViewLifecycleOwner());
        binding.rvRecipeList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipeList.setAdapter(adapter);
    }

    /**
     * Sets the loading data state as true, so the user won't be able to fetch more data while it's
     * getting data already from the database, and shows the progress bar as visible in order to let
     * the user watch the progress.
     */
    private void setDataIsLoading() {
        favouritesViewModel.setLoadingData(true);
        binding.pbProgress.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the loading data state as false, the data is fetched so the user will be able to get more
     * data, and the progress bar visibility will be hid.
     */
    private void setDataIsRetrieved() {
        favouritesViewModel.setLoadingData(false);
        binding.pbProgress.setVisibility(View.GONE);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}