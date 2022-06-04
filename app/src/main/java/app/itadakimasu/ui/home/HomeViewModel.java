package app.itadakimasu.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.FavouritesRepository;
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.data.repository.SharedPrefRepository;

/**
 * Sate holder for the HomeFragment.
 */
public class HomeViewModel extends AndroidViewModel {
    private final RecipesRepository recipesRepository;
    private final FavouritesRepository favouritesRepository;
    private final SharedPrefRepository sharedPrefRepository;


    private final MutableLiveData<List<Recipe>> recipeList;

    private boolean loadingData;
    // Boolean used to check when the user's retrieve a list when paginating and it returns nothing.
    // This one should be checked to false when the list is reloaded.
    private boolean reachedEndPagination;
    // Synchronized flag that is used to add the list retrieved when every recipe has been checked
    // if its marked as favourite
    private int recipesToProcessFlag;


    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.recipesRepository = RecipesRepository.getInstance();
        this.favouritesRepository = FavouritesRepository.getInstance();
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.recipeList = new MutableLiveData<>(new ArrayList<>());
    }


    /**
     * @return the list of the recipes stored on the view model.
     */
    public LiveData<List<Recipe>> getRecipeList() {
        return recipeList;
    }

    /**
     * @return the newest recipes fetched from the database.
     */
    public LiveData<Result<?>> loadFirstRecipes() {
        return recipesRepository.getNewestRecipes();
    }

    /**
     * Pagination of the recipes with no filter.
     * @return more recipes starting from the last recipe fetched previously.
     */
    public LiveData<Result<?>> loadNextRecipes() {
        Date lastRecipeDate = getRecipeAt(getListSize() - 1).getCreationDate();
        return recipesRepository.getNextRecipes(lastRecipeDate);
    }

    /**
     * Checks if a document with the user's authenticated username and the recipe's id exists in favourites collection.
     * @param recipe - the recipe's data to check if its one of the user's favourite.
     * @return observable result that contains a boolean if the entry has been found or not; false if an error happens.
     */
    public LiveData<Result<?>> isRecipeFavourite(Recipe recipe) {
        return favouritesRepository.findFavouriteRecipe(getAuthUsername(), recipe.getId());
    }

    /**
     * Add recipes to favourite.
     * @param recipeToFav - recipe to add to favourite.
     * @return success result with  if its added; error if something wrong happened.
     */
    public LiveData<Result<?>> addRemoveFavourite(Recipe recipeToFav) {
        if (recipeToFav.isFavourite()) {
            return favouritesRepository.removeFromFavourites(getAuthUsername(), recipeToFav.getId());
        }
        return favouritesRepository.addToFavourites(getAuthUsername(), recipeToFav.getId());
    }

    /**
     * Obtains recipes from given position.
     * @param position - the position where the recipe will be retrieved.
     * @return a recipe from the view model's list.
     */
    public Recipe getRecipeAt(int position) {
        return recipeList.getValue().get(position);
    }

    /**
     * @return the size of the list of recipes.
     */
    public int getListSize() {
        return recipeList.getValue().size();
    }

    /**
     * @return true if the recipe's list is empty; false if not.
     */
    public boolean isListEmpty() {
        return recipeList.getValue().isEmpty();
    }


    /**
     * @return true if data is being added or removed from the database; false if not.
     */
    public boolean isLoadingData() {
        return loadingData;
    }

    /**
     * Boolean used to disable pagination when the user tries to paginate data and the list doesn't
     * return any item, because the list reached its end.
     * @return true if after paginating the result of the list is empty; false if not.
     */
    public boolean reachedEndPagination() {
        return reachedEndPagination;
    }

    /**
     * Add new recipes on the list.
     * @param retrievedList - the new recipes that have been retrieved.
     */
    public void addRetrievedRecipes(List<Recipe> retrievedList) {
        if (!retrievedList.isEmpty()) {
            List<Recipe> list = recipeList.getValue();
            assert list != null;
            list.addAll(retrievedList);

            recipeList.setValue(list);
        }
    }

    /**
     * Clears the recipe list.
     */
    public void clearRecipeList() {
        recipeList.getValue().clear();
    }

    /**
     * Sets or unsets a recipe as a favourite, using the position of the recipe to mark and a boolean
     * that tells if the recipe will be marked as favourite (true) or not (false).
     * @param position - position of the recipe to change the favourite state.
     */
    public void markRecipeFavouriteAt(int position) {
        List<Recipe> updatedRecipeList = recipeList.getValue();
        assert updatedRecipeList != null;

        Recipe recipe = updatedRecipeList.get(position);
        recipe.setFavourite(!recipe.isFavourite());
        updatedRecipeList.set(position, recipe);

        recipeList.setValue(updatedRecipeList);
    }

    /**
     * Sets the boolean that checks if the user can't paginate more because there are no more recipes
     * to fetch.
     * @param state - the new state: true if there is no more entries to fetch; false if else.
     */
    public void setReachedEndPaginationState(boolean state) {
        this.reachedEndPagination = state;
    }

    /**
     * Sets the quantity of recipes that must be checked if its on favourites collection corresponding
     * with the authenticated user.
     * @param quantity - the size of the list retrieved from the database.
     */
    public void setRecipesToProcess(int quantity) {
        this.recipesToProcessFlag = quantity;
    }

    /**
     * Sets the state of data loading.
     * @param state - boolean state, use true when data is being processed from the database, false when it finish.
     */
    public void setLoadingDataState(boolean state) {
        this.loadingData = state;
    }

    /**
     * Synchronous method that decreases the number of recipes to check as favourite and returns what is left.
     * @return the quantity of recipes after one being processed.
     */
    public synchronized int recipeIsProcessed() {
        recipesToProcessFlag -= 1;
        return recipesToProcessFlag;
    }

    /**
     * @return the authenticated user's username.
     */
    public String getAuthUsername() {
        return sharedPrefRepository.getAuthUsername();
    }
}