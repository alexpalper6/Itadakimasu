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
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;

/**
 * ViewModel used to store the selected recipe data and show it to the user, surviving configuration
 * changes.
 */
public class RecipeDetailsViewModel extends AndroidViewModel {
    private final SharedPrefRepository sharedPrefRepository;
    private final StorageRepository storageRepository;
    private final RecipesRepository recipesRepository;

    private final MutableLiveData<Recipe> selectedRecipe;
    private final MutableLiveData<List<Ingredient>> ingredientList;
    private final MutableLiveData<List<Step>> stepList;

    public RecipeDetailsViewModel(@NonNull Application application) {
        super(application);
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.storageRepository = StorageRepository.getInstance();
        this.recipesRepository = RecipesRepository.getInstance();
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

    public LiveData<List<Ingredient>> getIngredientList() {
        return ingredientList;
    }

    public LiveData<List<Step>> getStepList() {
        return stepList;
    }

    public LiveData<Result<?>> loadIngredientList(String recipeId) {
        return recipesRepository.getIngredientsFromRecipe(recipeId);
    }

    public LiveData<Result<?>> loadStepsList(String recipeId) {
        return recipesRepository.getStepsFromRecipe(recipeId);
    }
    /**
     * Sets the recipe data when it's been received on the ParentFragmentManager result listener.
     * @param recipe - The reciped passed as argument thorugh the ParentFragmentManager.
     */
    public void setSelectedRecipe(Recipe recipe) {
        this.selectedRecipe.setValue(recipe);
    }

    public void setIngredientList(List<Ingredient> ingredientList) {
        this.ingredientList.setValue(ingredientList);
    }

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