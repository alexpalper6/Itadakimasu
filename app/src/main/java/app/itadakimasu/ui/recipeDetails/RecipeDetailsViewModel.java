package app.itadakimasu.ui.recipeDetails;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.model.Step;
import app.itadakimasu.data.repository.FavouritesRepository;
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;

/**
 * ViewModel used to store the selected recipe data and show it to the user, surviving configuration
 * changes.
 */
public class RecipeDetailsViewModel extends AndroidViewModel {
    // Repository used to obtain authenticated user's data.
    private final SharedPrefRepository sharedPrefRepository;
    // Repositories used to manage entries on the source data.
    private final StorageRepository storageRepository;
    private final RecipesRepository recipesRepository;
    private final FavouritesRepository favouritesRepository;
    // Observable data of the recipe's data.
    private final MutableLiveData<Recipe> selectedRecipe;
    private final MutableLiveData<List<Ingredient>> ingredientList;
    private final MutableLiveData<List<Step>> stepList;

    public RecipeDetailsViewModel(@NonNull Application application) {
        super(application);
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.storageRepository = StorageRepository.getInstance();
        this.recipesRepository = RecipesRepository.getInstance();
        this.favouritesRepository = FavouritesRepository.getInstance();
        this.selectedRecipe = new MutableLiveData<>();
        this.ingredientList = new MutableLiveData<>();
        this.stepList = new MutableLiveData<>();
    }


    /**
     * @return The observable recipe's data.
     */
    public LiveData<Recipe> getSelectedRecipe() {
        return selectedRecipe;
    }

    /**
     * @return the list of the recipe's ingredients.
     */
    public LiveData<List<Ingredient>> getIngredientList() {
        return ingredientList;
    }

    /**
     * @return the list of the recipe's steps.
     */
    public LiveData<List<Step>> getStepList() {
        return stepList;
    }

    /**
     * Loads the ingredients of the recipe using its id.
     * @param recipeId - the recipe id, that identifies the document entry in the collection.
     * @return the list of the recipe's ingredients, or an error as an observable data.
     */
    public LiveData<Result<?>> loadIngredientList(String recipeId) {
        return recipesRepository.getIngredientsFromRecipe(recipeId);
    }

    /**
     * Loads the step of the recipe using its id.
     * @param recipeId - the recipe id, that identifies the document entry in the collection.
     * @return the list of the recipe's steps, or an error as an observable data.
     */
    public LiveData<Result<?>> loadStepsList(String recipeId) {
        return recipesRepository.getStepsFromRecipe(recipeId);
    }

    /**
     * Adds the recipe to the user's favourites, using their authenticated user's username and the recipe id.
     * @return result success if it's added; error if else.
     */
    public LiveData<Result<?>> addRecipeToFavourites() {
        return favouritesRepository.addToFavourites(getAuthUsername(), selectedRecipe.getValue().getId());
    }

    /**
     * Removes the recipe to the user's favourites, using their authenticated user's username and the recipe id.
     * @return result success if it's added; error if else.
     */
    public LiveData<Result<?>> removeRecipeFromFavourites() {
        return favouritesRepository.removeFromFavourites(getAuthUsername(), selectedRecipe.getValue().getId());
    }

    /**
     * Checks if the user has this recipe added as favourite, using the auth user's username and recipe's id.
     * @return boolean true if is favourite, false if not; error if something wrong happens.
     */
    public LiveData<Result<?>> isRecipeFavourite() {
        return favouritesRepository.findFavouriteRecipe(getAuthUsername(), selectedRecipe.getValue().getId());
    }

    /**
     * @param imageUrl - the image path to download.
     * @return success uri with the image's data or result error if it fails.
     */
    public LiveData<Result<?>> downloadImageData(String imageUrl) {
        return storageRepository.getImageUri(imageUrl);
    }

    /**
     * Sets the recipe data when it's been received on the ParentFragmentManager result listener.
     * @param recipe - The reciped passed as argument thorugh the ParentFragmentManager.
     */
    public void setSelectedRecipe(Recipe recipe) {
        this.selectedRecipe.setValue(recipe);
    }

    /**
     * Sets the list of ingredients.
     * @param ingredientList - the list of ingredients that will be added.
     */
    public void setIngredientList(List<Ingredient> ingredientList) {
        this.ingredientList.setValue(ingredientList);
    }

    /**
     * Sets the list of steps.
     * @param stepList - the list of steps that will be added.
     */
    public void setStepList(List<Step> stepList) {
        this.stepList.setValue(stepList);
    }

    /**
     * Obtains the Storage Reference from given url.
     * @param imageUrl - The url that holds the image's data on StorageReference from firebase.
     * @return the storage reference, that will let the system to load the image on their respective ImageView.
     */
    public StorageReference getImageReference(String imageUrl) {
        return storageRepository.getImageReference(imageUrl);
    }

    /**
     * @return the authenticated user, used to remove favourites checkbox if the recipe author is the same.
     */
    public String getAuthUsername() {
        return sharedPrefRepository.getAuthUsername();
    }
}