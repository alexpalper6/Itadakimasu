package app.itadakimasu.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.databinding.FragmentHomeBinding;

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

        adapter = new HomeRecipesAdapter(requireContext());
        binding.rvRecipeList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipeList.setAdapter(adapter);

        binding.srlRefresh.setColorSchemeResources(R.color.primaryColor);

        // When the list is empty (the view model is created along with the fragment) then it fetches
        // the newest recipes.
        if (homeViewModel.isListEmpty()) {
            loadNewestRecipes();
        }




        // Query that filters by recipe's title using the data.

        binding.svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.w("Query: ", String.valueOf(query.trim().length() == 0));
                homeViewModel.setSearchCondition(query.trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Nothing to do.
                return false;
            }
        });

        // Observable that submit the list on the adapter when the list changes.
        homeViewModel.getRecipeList().observe(getViewLifecycleOwner(), recipeList -> adapter.submitList(new ArrayList<>(recipeList)));


    }

    /**
     * Loads the first newest recipes on the list, when the list is fetched, then every recipe inside
     * the list will be checked if its a favourite one from the user.
     */
    private void loadNewestRecipes() {
        homeViewModel.setLoadingDataState(true);
        binding.pbProgress.setVisibility(View.VISIBLE);

        homeViewModel.loadFirstRecipes().observe(getViewLifecycleOwner(), result -> {
            homeViewModel.setLoadingDataState(false);
            binding.pbProgress.setVisibility(View.GONE);

            if (result instanceof Result.Success) {

                List<Recipe> recipeList = ((Result.Success<List<Recipe>>) result).getData();
                checkForFavouriteRecipes(recipeList);

            } else {
                Snackbar.make(binding.getRoot(), R.string.ingredient_list_load_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Iterates the list of recipes, and for eeach one checks if the authenticated user added it as favourite,
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}