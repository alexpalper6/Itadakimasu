package app.itadakimasu.ui.recipeCreation;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.model.Step;
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;

/**
 * Shared ViewModel used on recipe, ingredients and steps fragments for creating a recipe.
 */
public class CreationViewModel extends AndroidViewModel {
    // Repositories to load, modify and add data from the database.
    private final RecipesRepository recipesRepository;
    private final StorageRepository storageRepository;
    // This repository is used to obtain the data from the shared preferences.
    private final SharedPrefRepository sharedPrefRepository;

    // The list of ingredients of the recipe, that will store the ingredients and show it to the user.
    private final MutableLiveData<List<Ingredient>> ingredientList;
    // The list of steps of the recipe, that will store the steps and show it to the user.
    private final MutableLiveData<List<Step>> stepList;
    // Photo uri that observes for changes to update the UI.
    private final MutableLiveData<Uri> photoUri;
    // Photo path where it will be uploaded to the storage.
    private String photoPath;
    private int itemPositionToEdit;
    // If this is null then there is no recipe to edit
    private String recipeIdToEdit;
    // Saves the date when the recipe to edit was created, this data won't be updated.
    private Date recipeDateToEdit;

    public CreationViewModel(@NonNull Application application) {
        super(application);
        this.recipesRepository = RecipesRepository.getInstance();
        this.storageRepository = StorageRepository.getInstance();
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.ingredientList = new MutableLiveData<>(new ArrayList<>());
        this.stepList = new MutableLiveData<>(new ArrayList<>());
        this.photoUri = new MutableLiveData<>();
    }

    /**
     * Uploads a recipe to the database, given the data that the user has input and its username and
     * photo.
     *
     * @return result success with the recipe image url to upload to sotrage; result error if it fails.
     */
    public LiveData<Result<?>> uploadRecipe(String author, String photoAuthorUrl,String recipeTitle, String recipeDescription) {
        Recipe recipe = new Recipe(author, photoAuthorUrl, recipeTitle, recipeDescription);

        return recipesRepository.uploadRecipe(recipe, getIngredientListToUpload(), getStepListToUpload());

    }

    /**
     * Updates an existed recipe's image, title and description, the edited recipe's id and date
     * must be passed as parameter.
     *
     * @return result success with the recipe image url to upload to sotrage; result error if it fails.
     */
    public LiveData<Result<?>> updateRecipe(String author, String photoAuthorUrl, String recipeTitle, String recipeDescription) {
        Recipe recipe = new Recipe(author, photoAuthorUrl, recipeTitle, recipeDescription);

        return recipesRepository.updateRecipe(recipeIdToEdit, recipeDateToEdit, recipe);

    }

    /**
     * Uploads to the storage the recipe image's data on given photo url.
     * @param recipePhotoUrl - the url path where the image will be uploaded.
     * @param imageData - the image compressed data.
     * @return result success if it's uploaded successfully; error if not.
     */
    public LiveData<Result<?>> uploadPhotoStorage(String recipePhotoUrl, byte[] imageData) {
        return storageRepository.updateRecipeImage(recipePhotoUrl, imageData);
    }

    /**
     * Downloads the image data as a uri.
     * @param imageUrl - the image path where the image will be retrieved from the storage.
     * @return result success with image's uri if successful; error if not.
     */
    public LiveData<Result<?>> getEditedRecipeImage(String imageUrl) {
        return storageRepository.getImageUri(imageUrl);
    }

    /**
     * @return the ingredient's list from the view model.
     */
    public LiveData<List<Ingredient>> getIngredientList() {
        return ingredientList;
    }

    /**
     * @return the step's list from the view model.
     */
    public LiveData<List<Step>> getStepList() {
        return stepList;
    }


    /**
     * @return the photo uri, used to change the ImageView UI from the fragment.
     */
    public LiveData<Uri> getPhotoUri() {
        return photoUri;
    }




    // Methods for ingredient list

    /**
     * Adds an ingredient to the ingredient list but first checks if the ingredients already exists,
     * if so, returns false, indicating that the ingredient couldn't be added. If its added, it will
     * return  true.
     * @param ingredientDescription - The ingredient's description.
     * @return true if the ingredient is added; false if an ingredient already has the given description.
     */
    public boolean addIngredient(String ingredientDescription) {
        if (ingredientExists(ingredientDescription)) {
            return false;
        }
        Ingredient ingredient = new Ingredient( ingredientDescription);
        List<Ingredient> list = ingredientList.getValue();
        list.add(ingredient);

        ingredientList.setValue(list);

        return true;
    }

    /**
     * Given the position of the ingredient to edit and the modified description introduced by the user,
     * update the ingredient on the list.
     * @param description - the new description of the ingredient.
     * @return - false if an ingredient already has the description; true if the ingredient is edited.
     */
    public boolean editIngredient(String description) {
        if (ingredientExists(description)) {
            return false;
        }
        List<Ingredient> list = ingredientList.getValue();
        list.set(itemPositionToEdit, new Ingredient(description));

        ingredientList.setValue(list);
        return true;
    }

    /**
     * Removes ingredient from the list giving its position;
     * @param ingredientPosition - The position of the ingredient to remove.
     */
    public void removeIngredientAt(int ingredientPosition) {
        assert ingredientList.getValue() != null;
        ingredientList.getValue().remove(ingredientPosition);
        ingredientList.setValue(ingredientList.getValue());
    }

    /**
     * Checks if an ingredient exists on the list searching for its description.
     * @param ingredientDescription - The description that will used to check if another ingredient has it.
     * @return true if an ingredient has the description; false if not.
     */
    public boolean ingredientExists(String ingredientDescription) {
        assert ingredientList.getValue() != null;
        return ingredientList.getValue().contains(new Ingredient(ingredientDescription));
    }

    /**
     * Checks if the user ingredient's edited description is the same as the old description.
     * @param description - The ingredient's description edited.
     * @return true if the new description is different; false if is the same.
     */
    public boolean ingredientHasDifferentDescToEdit(String description) {
        assert ingredientList.getValue() != null;
        return !ingredientList.getValue().get(itemPositionToEdit).getIngredientDescription().equals(description);
    }



    // Methods for step list

    /**
     * Tries to add a new step with given description. It will return a boolean, indicating if the
     * step couldn't be added because another has the same description or if it could be added.
     * @param stepDescription - The step description given by the user.
     * @return false if a step already exists; true if its added to the step's list.
     */
    public boolean addStep(String stepDescription) {
        if (stepExists(stepDescription)) {
            return false;
        }
        assert stepList.getValue() != null;
        Step step = new Step(stepDescription);
        List<Step> list = stepList.getValue();
        list.add(step);

        stepList.setValue(list);

        return true;
    }

    /**
     * Given the position of the step to edit and the modified description introduced by the user,
     * update the step on the list.
     * @param description - the new description for the step.
     * @return - false if a step already has the description; true if the step is edited.
     */
    public boolean editStep(String description) {
        if (stepExists(description)) {
            return false;
        }
        List<Step> list = stepList.getValue();
        assert list != null;
        list.set(itemPositionToEdit, new Step(description));

        stepList.setValue(list);
        return true;
    }

    /**
     * Removes a step from the list by a given position.
     * @param stepPosition - The position of the step to remove.
     */
    public void removeStepAt(int stepPosition) {
        assert stepList.getValue() != null;
        stepList.getValue().remove(stepPosition);
        stepList.setValue(stepList.getValue());
    }

    /**
     * Checks if the step exists by searching for a step with the description that the user has given.
     * @param stepDescription - The description given by the user.
     * @return true if a step have been found with the same description; false if not.
     */
    public boolean stepExists(String stepDescription) {
        assert stepList.getValue() != null;
        return stepList.getValue().contains(new Step(stepDescription));
    }

    /**
     * Checks if the user step's edited description is the same as the old description.
     * @param description - The step's description edited.
     * @return true if the new description is different; false if is the same.
     */
    public boolean stepHasDifferentDescToEdit(String description) {
        assert stepList.getValue() != null;
        return !stepList.getValue().get(itemPositionToEdit).getStepDescription().equals(description);
    }

    // ------- //

    /**
     * @return the path of the cropped image that the user uploads.
     */
    public String getPhotoPath() {
        return photoPath;
    }

    /**
     * Sets the image path that has been cropped.
     * @param photoPath - the cropped image path.
     */
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    /**
     * Sets the uri of the cropped image.
     * @param uri - the cropped image.
     */
    public void setPhotoUri(Uri uri) {
        photoUri.setValue(uri);
    }

    /**
     * @param itemPositionToEdit - the position of the item selected that will be used for editing or
     *                           removing an item from a list.
     */
    public void setItemPositionToEdit(int itemPositionToEdit) {
        this.itemPositionToEdit = itemPositionToEdit;
    }

    /**
     * Checks if all fields are filled.
     * @param recipeTitle - the recipe's title EditText from the RecipeCreationFragment.
     * @param recipeDescription - the recipe's description EditText from the RecipeCreationFragment.
     * @return true if every field is filled; false if not.
     */
    public boolean areFieldsFilled(String recipeTitle, String recipeDescription) {
        assert ingredientList.getValue() != null;
        assert stepList.getValue() != null;

        boolean titleIsFilled = recipeTitle.length() != 0;
        boolean descriptionIsFilled = recipeDescription.length() != 0;
        boolean photoFieldFilled = (photoPath != null && photoPath.length() != 0) && photoUri.getValue() != null;
        boolean listFieldsFilled = !ingredientList.getValue().isEmpty()
                && !stepList.getValue().isEmpty();

        return titleIsFilled && descriptionIsFilled && photoFieldFilled && listFieldsFilled;
    }

    /**
     * @return a list of the ingredients that are going to be uploaded, with their position updated.
     */
    private List<Ingredient> getIngredientListToUpload() {
        List<Ingredient> list = ingredientList.getValue();
        assert list != null;
        for (int i = 0; i < list.size(); i++) {
            list.get(0).setIngredientPosition(i);
        }

        return list;
    }

    /**
     *
     * @return a list of the steps that are going to be uploaded, with their position updated.
     */
    private List<Step> getStepListToUpload() {
        List<Step> list = stepList.getValue();
        assert list != null;
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setStepPosition(i);
        }
        return list;
    }

    /**
     * @return the authenticated user's username.
     */
    public String getAuthUsername() {
        return sharedPrefRepository.getAuthUsername();
    }

    /**
     * @return the authenticated user's photo url.
     */
    public String getAuthUserPhotoUrl() {
        return sharedPrefRepository.getAuthUserPhotoUrl();
    }

    /**
     * @return the recipe's id to edit, if it returns null, then the recipe is new, is not editable.
     */
    public String getRecipeIdToEdit() {
        return recipeIdToEdit;
    }

    /**
     * Sets the recipe's id to edit.
     * @param recipeId - the recipe's id to edit, obtained from a result fragment.
     */
    public void setRecipeIdToEdit(String recipeId) {
        this.recipeIdToEdit = recipeId;
    }

    /**
     * @return the recipe's date that is edited.
     */
    public Date getRecipeDateToEdit() {
        return recipeDateToEdit;
    }

    /**
     * Sets the recipe's date that is being edited, this is used because when the recipe is edited,
     * the recipe creation date will remain the same when it was created for the first time.
     * @param recipeDateToEdit - the recipe's date that is being edited.
     */
    public void setRecipeDateToEdit(Date recipeDateToEdit) {
        this.recipeDateToEdit = recipeDateToEdit;
    }



}