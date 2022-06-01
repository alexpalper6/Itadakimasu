package app.itadakimasu.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
import app.itadakimasu.ui.profile.ProfileFragment;
import app.itadakimasu.ui.recipeDetails.RecipeDetailsFragment;

/**
 * Main section fragment of the app, loads the newest recipes from every user.
 * It also has a search view that lets filter by title.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private HomeRecipesAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setAdapterRecyclerView();
        binding.srlRefresh.setColorSchemeResources(R.color.primaryColor);

        // When the list is empty (the view model is created along with the fragment) then it fetches
        // the newest recipes.
       // if (homeViewModel.isListEmpty()) {
            loadNewestRecipes();
        //}

        // Observable that submit the list on the adapter when the list changes.
        homeViewModel.getRecipeList().observe(getViewLifecycleOwner(), recipeList -> adapter.submitList(new ArrayList<>(recipeList)));


        adapter.setOnClickShowProfileListener(itemPosition -> {
            Recipe userRecipe = homeViewModel.getRecipeAt(itemPosition);

            Bundle result = new Bundle();
            result.putString(ProfileFragment.RESULT_USERNAME, userRecipe.getAuthor());
            result.putString(ProfileFragment.RESULT_USER_PHOTO, userRecipe.getPhotoAuthorUrl());

            getParentFragmentManager().setFragmentResult(ProfileFragment.REQUEST, result);
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_navigation_profile);
        } );

        adapter.setOnClickDisplayListener(itemPosition -> {
            Bundle result = new Bundle();
            result.putParcelable(RecipeDetailsFragment.RESULT, homeViewModel.getRecipeAt(itemPosition));
            getParentFragmentManager().setFragmentResult(RecipeDetailsFragment.REQUEST, result);
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_navigation_details);
        });

        //adapter.setOnClickAddFavListener(itemPosition -> );

        // Listener for the refresh layout, when refreshed, it fetches the newest recipes.
        binding.srlRefresh.setOnRefreshListener(() -> {
                loadNewestRecipes();

        });

        binding.rvRecipeList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if ((!recyclerView.canScrollVertically(1)) && !homeViewModel.isLoadingData() && !homeViewModel.reachedEndPagination()) {
                    loadNextRecipes();

                }
            }
        });
    }

    private void setAdapterRecyclerView() {
        adapter = new HomeRecipesAdapter(requireContext());
        binding.rvRecipeList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipeList.setAdapter(adapter);
    }

    /**
     * Loads the first newest recipes on the list, when the list is fetched, then every recipe inside
     * the list will be checked if its a favourite one from the user.
     */
    private void loadNewestRecipes() {
        setDataIsLoading();
        homeViewModel.setReachedEndPaginationState(false);


        homeViewModel.loadFirstRecipes().observe(getViewLifecycleOwner(), result -> {
            setDataIsRetrieved();
            binding.srlRefresh.setRefreshing(false);

            if (result instanceof Result.Success) {

                List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                checkForNewestFavouriteRecipes(recipeList);

            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }



    private void loadNextRecipes() {
        if (homeViewModel.getListSize() % RecipesRepository.LIMIT_QUERY == 0) {
            setDataIsLoading();
            homeViewModel.loadNextRecipes().observe(getViewLifecycleOwner(), result -> {
                setDataIsRetrieved();

                if (result instanceof Result.Success) {
                    List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                    homeViewModel.setReachedEndPaginationState(recipeList.isEmpty());
                    checkForFavouriteRecipes(recipeList);
                } else {
                    Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, BaseTransientBottomBar.LENGTH_LONG).show();
                }
            });
        } else {
            homeViewModel.setReachedEndPaginationState(true);
        }
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
    private void checkForNewestFavouriteRecipes(List<Recipe> recipeList) {
        // Sets the quantity of recipes to process
        homeViewModel.setRecipesToProcess(recipeList.size());
        for (Recipe recipe : recipeList) {
            // If the recipe is from the same user as the authenticated, then it will just mark it as processed.
            if (!recipe.getAuthor().equals(homeViewModel.getAuthUsername())) {

                homeViewModel.isRecipeFavourite(recipe).observe(getViewLifecycleOwner(), favouriteResult -> {

                    if (favouriteResult instanceof Result.Success) {
                        recipe.setFavourite(((Result.Success<Boolean>) favouriteResult).getData());
                    }
                    // Its necessary to do this twice because the asynchronous methods could spend
                    // more time than expected.
                    if (homeViewModel.recipeIsProcessed() == 0) {
                        homeViewModel.setRecipeList(recipeList);
                    }

                });
            } else {
                if (homeViewModel.recipeIsProcessed() == 0) {
                    homeViewModel.setRecipeList(recipeList);
                }
            }
        }
    }

    /**
     * This method adds the recipes after being checked if the recipes are checked as favourite.
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}