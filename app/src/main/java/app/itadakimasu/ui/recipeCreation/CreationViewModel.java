package app.itadakimasu.ui.recipeCreation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.model.Step;

/**
 * Shared ViewModel used on recipe, ingredients and steps fragments for creating a recipe.
 */
public class CreationViewModel extends ViewModel {
    private MutableLiveData<Recipe> recipe;

    private MutableLiveData<List<Ingredient>> ingredientList;
    private MutableLiveData<List<Step>> stepList;

    private int itemPositionToEdit;
    public CreationViewModel() {
        this.recipe = new MutableLiveData<>();
        this.ingredientList = new MutableLiveData<>(new ArrayList<>());
        this.stepList = new MutableLiveData<>(new ArrayList<>());
    }


    public LiveData<Recipe> getRecipe() {
        return recipe;
    }

    public LiveData<List<Step>> getStepList() {
        return stepList;
    }

    public LiveData<List<Ingredient>> getIngredientList() {
        return ingredientList;
    }

    public int getItemPositionToEdit() {
        return itemPositionToEdit;
    }

    public void setItemPositionToEdit(int itemPositionToEdit) {
        this.itemPositionToEdit = itemPositionToEdit;
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
        ingredientList.getValue().remove(ingredientPosition);
        ingredientList.setValue(ingredientList.getValue());
    }

    /**
     * Checks if an ingredient exists on the list searching for its description.
     * @param ingredientDescription - The description that will used to check if another ingredient has it.
     * @return true if an ingredient has the description; false if not.
     */
    public boolean ingredientExists(String ingredientDescription) {
        return ingredientList.getValue().contains(new Ingredient(ingredientDescription));
    }

    /**
     * Checks if the user ingredient's edited description is the same as the old description.
     * @param description - The ingredient's description edited.
     * @return true if the new description is different; false if is the same.
     */
    public boolean ingredientHasDifferentDescToEdit(String description) {
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
        list.set(itemPositionToEdit, new Step(description));

        stepList.setValue(list);
        return true;
    }

    /**
     * Removes a step from the list by a given position.
     * @param stepPosition - The position of the step to remove.
     */
    public void removeStepAt(int stepPosition) {
        stepList.getValue().remove(stepPosition);
        stepList.setValue(stepList.getValue());
    }

    /**
     * Checks if the step exists by searching for a step with the description that the user has given.
     * @param stepDescription - The description given by the user.
     * @return true if a step have been found with the same description; false if not.
     */
    public boolean stepExists(String stepDescription) {
        return stepList.getValue().contains(new Step(stepDescription));
    }

    /**
     * Checks if the user step's edited description is the same as the old description.
     * @param description - The step's description edited.
     * @return true if the new description is different; false if is the same.
     */
    public boolean stepHasDifferentDescToEdit(String description) {
        return !stepList.getValue().get(itemPositionToEdit).getStepDescription().equals(description);
    }

}