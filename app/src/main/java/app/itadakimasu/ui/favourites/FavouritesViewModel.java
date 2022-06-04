package app.itadakimasu.ui.favourites;

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
 * View model that stores the data that uses favourites fragment.
 */
public class FavouritesViewModel extends AndroidViewModel {
    // Repositories used to manage data on the data sources.
    private final RecipesRepository recipesRepository;
    private final FavouritesRepository favouritesRepository;
    private final SharedPrefRepository sharedPrefRepository;

    // Mutable and observable list of recipes.
    private final MutableLiveData<List<Recipe>> recipeList;
    // Last favourite entry date used for pagination.
    private Date lastFavDate;
    // State to check if data is being loaded or not.
    private boolean loadingData;
    // State used to know if there are no more data to paginate.
    private boolean reachedEndPagination;
    // Quantity of recipes that have to be loaded, after receiving the favourites entries.
    private int recipesToProcess;

    public FavouritesViewModel(@NonNull Application application) {
        super(application);
        this.recipesRepository = RecipesRepository.getInstance();
        this.favouritesRepository = FavouritesRepository.getInstance();
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.recipeList = new MutableLiveData<>(new ArrayList<>());
    }


    /**
     * @return the list of recipes to observe.
     */
    public LiveData<List<Recipe>> getRecipeList() {
        return recipeList;
    }

    /**
     * @return the first newest favourites entries from the authenticated user.
     */
    public LiveData<Result<?>> loadNewestFavourites() {
        return favouritesRepository.getNewestFavouritesByUser(getAuthUsername());
    }

    /**
     * @return the next favourites entries from the authenticated user, using the last favourite
     * entry date as the point for pagination.
     */
    public LiveData<Result<?>> loadNextFavourites() {
        return favouritesRepository.getNextFavouritesByUser(getAuthUsername(), lastFavDate);
    }

    /**
     * Removes a favourite entry from the document, using the authenticated user's username
     * and the recipe's id at given position as data.
     * @param recipePosition - the position of the recipe to get the id.
     * @return an observable result , success if it was removed successfully or error if not.
     */
    public LiveData<Result<?>> removeFavourite(int recipePosition) {
        return favouritesRepository.removeFromFavourites(getAuthUsername(), getRecipeAt(recipePosition).getId());
    }

    /**
     * Obtains the data of a recipe.
     * @param recipeId - the recipe's id from which the data will be fetched.
     * @return an observable result with the recipe's data or an error.
     */
    public LiveData<Result<?>> loadRecipeData(String recipeId) {
        return recipesRepository.getRecipeById(recipeId);
    }

    /**
     * @return true if the list is empty; false if not.
     */
    public boolean isListEmpty() {
        return recipeList.getValue().isEmpty();
    }

    /**
     * @param itemPosition - the position of the recipe we want to get on the list.
     * @return the recipe's data from the list.
     */
    public Recipe getRecipeAt(int itemPosition) {
        return recipeList.getValue().get(itemPosition);
    }

    /**
     * @return true if data is loading (ie: obtaining data from the database, or removing); false if not.
     */
    public boolean isLoadingData() {
        return loadingData;
    }

    /**
     * @return true if there is no more entries to fetch; false if else.
     */
    public boolean reachedEndPagination() {
        return reachedEndPagination;
    }

    /**
     * Synchronized method to decrease by 1 the quantity of recipes remaining to load.
     * @return - the quantity of recipes to be loaded after being decreased.
     */
    public synchronized int recipeProcessed() {
        recipesToProcess -= 1;
        return recipesToProcess;
    }

    /**
     * Add recipes to the list.
     * @param retrievedList - the list of the recipes with their data loaded.
     */
    public void addRecipes(List<Recipe> retrievedList) {
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
     * Remove a recipe on the list from given position.
     * @param recipePosition - the recipe's position to delete.
     */
    public void removeRecipeAt(int recipePosition) {
        List<Recipe> list = recipeList.getValue();
        assert list != null;
        list.remove(recipePosition);

        recipeList.setValue(list);
    }

    /**
     * Sets data loading state.
     * @param state true if there is data loading (ie: fetching data from the database or removing); false if not.
     */
    public void setLoadingData(boolean state) {
        loadingData = state;
    }

    /**
     * Sets reached end pagination state.
     * @param state - true if there are no more entries to fetch; false if else.
     */
    public void setReachedEndPagination(boolean state) {
        reachedEndPagination = state;
    }

    /**
     * Sets the last favourite date to use it as pagination point.
     * @param lastFavDate - the last favourite entry's date.
     */
    public void setLastFavDate(Date lastFavDate) {
        this.lastFavDate = lastFavDate;
    }

    /**
     * Sets the quantity of recipes to load.
     * @param quantity - the size of how many recipes must be loaded.
     */
    public void setRecipesToProcess(int quantity) {
        recipesToProcess = quantity;
    }

    /**
     * @return the authenticated user's username.
     */
    public String getAuthUsername() {
        return sharedPrefRepository.getAuthUsername();
    }

}