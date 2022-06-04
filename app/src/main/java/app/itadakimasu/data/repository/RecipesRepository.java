package app.itadakimasu.data.repository;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.model.Step;

/**
 * Repository that creates, retrieves and delete recipes from firebase's database.
 */
public class RecipesRepository {
    private static final String TAG = "RecipesRepository";
    // Repository's singleton
    public static volatile RecipesRepository INSTANCE;
    // Limit of how many documents will be fetched per query.
    public static int LIMIT_QUERY = 10;
    // Data source of firestore database.
    private final FirebaseFirestore dbFirestore;

    /**
     * @return the singleton of the repository.
     */
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

    /**
     * Sets the firebase firestore data source instance.
     */
    private RecipesRepository() {
        this.dbFirestore = FirebaseFirestore.getInstance();
    }

    /**
     * Uploads a recipe with its ingredients and steps.
     * The image reference of the recipe image will be added to the recipe and returned as a successful
     * result, that contains the constant of the recipes pictures path for storage and the document id.
     * The timestamp will be added as well to the recipe, so every recipe can be sorted by creation
     * date.
     * @param recipe - the recipe that contains the author name, author image, title and description.
     * @param ingredientList -  the list of ingredients of the recipe.
     * @param stepList - the list of steps.
     * @return the recipe photo url path that will be used to upload it to the storage.
     */
    public LiveData<Result<?>> uploadRecipe(Recipe recipe, List<Ingredient> ingredientList, List<Step> stepList) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        WriteBatch batch = dbFirestore.batch();
        Log.i(TAG, "uploadRecipe: creating recipe's document");
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
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "uploadRecipe: error uploading recipe's entry", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }

    /**
     * Update a recipe, for now, it won't be able to update the lists.
     * The image reference of the recipe image will be added to the recipe and return as a successful
     * return using the constant of the recipes pictures path for storage and the document id.
     * The timestamp will be added as well to the recipe, so every recipe can be sorted by creation
     * date.
     * @param recipeIdToEdit - the recipe's id that is being edited.
     * @param recipeDateToEdit - the recipe's date that is being edited, the date will remain the same as when it was created.
     * @param recipe - the recipe that contains the author name, author image, and the edited title and description.
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
                            .addOnFailureListener(failure -> {
                                Log.e(TAG, "updateRecipe: error updating recipe's entry", failure);
                                result.setValue(new Result.Error(failure));
                            });

        return result;
    }

    /**
     * Deletes recipe with given id.
     * @param recipeId - the recipe's id.
     * @return Result.Success if the recipe is deleted; Result.Error if it fails.
     */
    public LiveData<Result<?>> deleteRecipe(String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "deleteRecipe: deleting recipe's entry");
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document(recipeId).delete()
                .addOnSuccessListener(success -> result.setValue(new Result.Success<Object>(null)))
                .addOnFailureListener(failure ->  {
                    Log.e(TAG, "deleteRecipe: error deleting recipe's entry", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }

    /**
     * Obtains a recipe's data with given id.
     * @param recipeId - the recipe's id.
     * @return Result.Success with the recipe's data; error if something goes wrong.
     */
    public LiveData<Result<?>> getRecipeById(String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.d(TAG, "getRecipeById: obtaining recipe entry");
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Recipe recipe = documentSnapshot.toObject(Recipe.class);
                    result.setValue(new Result.Success<Recipe>(recipe));
                })
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "getRecipeById: error obtaining the document", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }

    /**
     * Obtains the first newest recipes sorted by creation date.
     * @return Result.Success with the list of recipes; error if something goes wrong.
     */
    public LiveData<Result<?>> getNewestRecipes() {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "getNewestRecipes: obtaining newest recipes' documents");
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.CREATION_DATE, Query.Direction.DESCENDING)
                .limit(LIMIT_QUERY)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Recipe>>(queryDocumentSnapshots.toObjects(Recipe.class))))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "getNewestRecipes: error obtaining the documents", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }

    /**
     * Given the last recipe's date from the user's recipe list loaded on memory, obtain the next recipes.
     * @param lastRecipeDate - the last recipe's creation date on the user's list on loaded on memory.
     * @return Result.Success with the list of recipes; Result.Error if something goes wrong.
     */
    public LiveData<Result<?>> getNextRecipes(Date lastRecipeDate) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "getNextRecipes: obtaining next documents");
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.CREATION_DATE, Query.Direction.DESCENDING)
                .startAfter(lastRecipeDate)
                .limit(LIMIT_QUERY)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        result.setValue(new Result.Success<List<Recipe>>(task.getResult().toObjects(Recipe.class)));
                    } else {
                        Log.e(TAG, "getNextRecipes: error obtaining next recipes' documents", task.getException());
                        result.setValue(new Result.Error(task.getException()));
                    }
                });

        return result;
    }



    /**
     * Obtains the first recipes of given username. The list is ordered by creation date and
     * the quantity of Recipes returned are limited with the constant LIMIT_QUERY.
     * @param username - the username from which their recipes will be retrieve.
     * @return Result.Success with the list if is successful; Result.Error if it fails to load.
     */
    public LiveData<Result<?>> getRecipesByUser(String username) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "getRecipesByUser: obtaining recipes documents by user");
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.RecipeEntry.AUTHOR, username)
                .orderBy(FirebaseContract.RecipeEntry.CREATION_DATE, Query.Direction.DESCENDING)
                .limit(LIMIT_QUERY)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Recipe>>(queryDocumentSnapshots.toObjects(Recipe.class))))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "getRecipesByUser: error obtaining the documents", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }


    /**
     * Paginates the next recipes from a user given their username and the las recipe's date of their list
     * loaded in memory. The query limits the quantity returned with LIMIT_QUERY.
     * @param profileUsername - the username which their recipes will be retrieved.
     * @param lastRecipeDate - the last recipe's date, will be used as a cursor for the pagination.
     * @return Result.Success with the list if is successful; Result.Error if it fails.
     */
    public LiveData<Result<?>> getNextRecipesByUser(String profileUsername, Date lastRecipeDate) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "getNextRecipesByUser: obtaining documents references");
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.RecipeEntry.AUTHOR, profileUsername)
                .orderBy(FirebaseContract.RecipeEntry.CREATION_DATE, Query.Direction.DESCENDING)
                .startAfter(lastRecipeDate)
                .limit(LIMIT_QUERY)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        result.setValue(new Result.Success<List<Recipe>>(task.getResult().toObjects(Recipe.class)));
                    } else {
                        Log.e(TAG, "getNextRecipesByUser: error obtaining the references", task.getException());
                        result.setValue(new Result.Error(task.getException()));
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
        Log.i(TAG, "getIngredientsFromRecipe: obtaining ingredients collection from recipe entry");
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document(recipeId)
                .collection(FirebaseContract.RecipeEntry.IngredientEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.IngredientEntry.POSITION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Ingredient>>(queryDocumentSnapshots.toObjects(Ingredient.class))))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "getIngredientsFromRecipe: error obtaining collection", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }

    /**
     * Obtains the steps of a recipe, ordered by its position.
     * @param recipeId - the recipe's id, used to identify the document.
     * @return a success result with the list if it loads correctly; a result error if it doesn't.
     */
    public LiveData<Result<?>> getStepsFromRecipe(String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "getStepsFromRecipe: obtaining steps collection from recipe entry");
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document(recipeId)
                .collection(FirebaseContract.RecipeEntry.StepEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.StepEntry.POSITION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Step>>(queryDocumentSnapshots.toObjects(Step.class))))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "getStepsFromRecipe: error obtaining collection", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }
}
