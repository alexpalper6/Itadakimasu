package app.itadakimasu.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.model.Step;
public class RecipesRepository {

    public static volatile RecipesRepository INSTANCE;
    public static int LIMIT_QUERY = 10;
    private final FirebaseFirestore dbFirestore;

    public static RecipesRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (RecipesRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RecipesRepository();
                }
            }
        }
        return INSTANCE;
    }


    private RecipesRepository() {
        this.dbFirestore = FirebaseFirestore.getInstance();
    }

    /**
     * Upload a recipe with its ingredients and steps.
     * The image reference of the recipe image will be added to the recipe and return as a successful
     * return using the constant of the recipes pictures path for storage and the document id.
     * The timestamp will be added as well to the recipe, so every recipe can be sorted by creation
     * date.
     * @param recipe the recipe that contains the author name, author image, title and description.
     * @param ingredientList the list of ingredients of the recipe
     * @param stepList the list of steps
     * @return the recipe photo url path that will be used to upload it to the storage.
     */
    public LiveData<Result<?>> uploadRecipe(Recipe recipe, List<Ingredient> ingredientList, List<Step> stepList) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        WriteBatch batch = dbFirestore.batch();

        DocumentReference recipeReference = dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document();
        recipe.setId(recipeReference.getId());
        recipe.setPhotoUrl(FirebaseContract.StoragePath.RECIPES_PICTURES + recipeReference.getId());
        // Sets the value of the recipe's document
        batch.set(recipeReference, recipe);

        // Creates documents reference for each ingredient
        for (Ingredient i : ingredientList) {
            DocumentReference ingredientReference = dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                    .document(recipeReference.getId()).collection(FirebaseContract.RecipeEntry.IngredientEntry.COLLECTION_NAME)
                    .document();
            batch.set(ingredientReference, i);
        }

        // Creates documents reference for each step
        for (Step s : stepList) {
            DocumentReference stepReference = dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                    .document(recipeReference.getId()).collection(FirebaseContract.RecipeEntry.StepEntry.COLLECTION_NAME)
                    .document();
            batch.set(stepReference, s);
        }

        batch.commit().addOnSuccessListener(success -> result.setValue(new Result.Success<String>(recipe.getPhotoUrl())))
                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));

        return result;
    }

    /**
     * Update a recipe, for now, it won't be able to update the lists.
     * The image reference of the recipe image will be added to the recipe and return as a successful
     * return using the constant of the recipes pictures path for storage and the document id.
     * The timestamp will be added as well to the recipe, so every recipe can be sorted by creation
     * date.
     *
     * @param recipeIdToEdit the recipe's id that is being edited.
     * @param recipeDateToEdit the recipe's date that is being edited, the date will remain the same as when it was created.
     * @param recipe the recipe that contains the author name, author image, and the edited title and description.
     * @return the recipe photo url path that will be used to upload it to the storage.
     */
    public LiveData<Result<?>> updateRecipe(String recipeIdToEdit,Date recipeDateToEdit, Recipe recipe) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

                    WriteBatch batch = dbFirestore.batch();
                    DocumentReference recipeReference = dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document(recipeIdToEdit);
                    batch.delete(recipeReference);

                    recipe.setId(recipeIdToEdit);
                    recipe.setCreationDate(recipeDateToEdit);
                    recipe.setPhotoUrl(FirebaseContract.StoragePath.RECIPES_PICTURES + recipeIdToEdit);
                    batch.set(recipeReference, recipe);


                    batch.commit().addOnSuccessListener(success -> result.setValue(new Result.Success<String>(recipe.getPhotoUrl())))
                            .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));


        return result;

    }

    /**
     * Deletes recipe with given id.
     * @param recipeId - the recipe's id.
     * @return Result.Success if the recipe is deleted; Result.Error if it fails.
     */
    public LiveData<Result<?>> deleteRecipe(String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document(recipeId).delete()
                .addOnSuccessListener(success -> result.setValue(new Result.Success<Object>(null)))
                .addOnFailureListener(failure ->  result.setValue(new Result.Error(failure)));

        return result;
    }

    public LiveData<Result<?>> getNewestRecipes() {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.CREATION_DATE, Query.Direction.DESCENDING)
                .limit(LIMIT_QUERY)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Recipe>>(queryDocumentSnapshots.toObjects(Recipe.class))))
                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));

        return result;
    }


    public LiveData<Result<?>> loadNextRecipes(Date lastRecipeDate) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.CREATION_DATE, Query.Direction.DESCENDING)
                .startAfter(lastRecipeDate)
                .limit(LIMIT_QUERY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            result.setValue(new Result.Success<List<Recipe>>(task.getResult().toObjects(Recipe.class)));
                        } else {
                            result.setValue(new Result.Error(task.getException()));
                        }
                    }
                });

        return result;
    }

    /**
     * Obtains the first recipes of given username. The list is ordered by creation date descending and
     * the quantity of Recipes returned are limited with the constant LIMIT_QUERY.
     * @param username - the username which their recipes will be retrieve.
     * @return Result.Success with the list if is successful; Result.Error if it fails to load.
     */
    public LiveData<Result<?>> getRecipesByUser(String username) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.RecipeEntry.AUTHOR, username)
                .orderBy(FirebaseContract.RecipeEntry.CREATION_DATE, Query.Direction.DESCENDING)
                .limit(LIMIT_QUERY)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Recipe>>(queryDocumentSnapshots.toObjects(Recipe.class))))
                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));

        return result;
    }


    /**
     * Paginates the next recipes from a user given their username and the las recipe's date of their list
     * loaded in memory. The query limits the quantity returned with LIMIT_QUERY.
     * @param profileUsername - the username which their recipes will be retrieved.
     * @param lastRecipeDate - the last recipe's date, will be used as a cursor for the pagination.
     * @return Result.Success with the list if is successful; Result.Error if it fails.
     */
    public LiveData<Result<?>> loadNextRecipesByUser(String profileUsername, Date lastRecipeDate) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.RecipeEntry.AUTHOR, profileUsername)
                .orderBy(FirebaseContract.RecipeEntry.CREATION_DATE, Query.Direction.DESCENDING)
                .startAfter(lastRecipeDate)
                .limit(LIMIT_QUERY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            result.setValue(new Result.Success<List<Recipe>>(task.getResult().toObjects(Recipe.class)));
                        } else {
                            result.setValue(new Result.Error(task.getException()));
                        }
                    }
                });

        return result;
    }

    /**
     * Obtains the ingredients of a recipe, ordered by its position.
     * @param recipeId - the recipe's id, used to identify the document.
     * @return a success result with the list if it loads correctly; a result error if it doesn't.
     */
    public LiveData<Result<?>> getIngredientsFromRecipe(String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document(recipeId)
                .collection(FirebaseContract.RecipeEntry.IngredientEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.IngredientEntry.POSITION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Ingredient>>(queryDocumentSnapshots.toObjects(Ingredient.class))))
                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));

        return result;
    }

    /**
     * Obtains the steps of a recipe, ordered by its position.
     * @param recipeId - the recipe's id, used to identify the document.
     * @return a success result with the list if it loads correctly; a result error if it doesn't.
     */
    public LiveData<Result<?>> getStepsFromRecipe(String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document(recipeId)
                .collection(FirebaseContract.RecipeEntry.StepEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.StepEntry.POSITION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Step>>(queryDocumentSnapshots.toObjects(Step.class))))
                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));

        return result;
    }


}
