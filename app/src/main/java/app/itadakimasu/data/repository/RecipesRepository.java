package app.itadakimasu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.model.Step;

public class RecipesRepository {

    public static volatile RecipesRepository INSTANCE;
    public static int LIMIT_QUERY = 10;
    private FirebaseFirestore dbFirestore;


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

        batch.set(recipeReference, recipe);

        for (Ingredient i : ingredientList) {
            DocumentReference ingredientReference = dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                    .document(recipeReference.getId()).collection(FirebaseContract.RecipeEntry.IngredientEntry.COLLECTION_NAME)
                    .document();
            batch.set(ingredientReference, i);
        }

        for (Step s : stepList) {
            DocumentReference stepReference = dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                    .document(recipeReference.getId()).collection(FirebaseContract.RecipeEntry.StepEntry.COLLECTION_NAME)
                    .document();
            batch.set(stepReference, s);
        }

        batch.commit().addOnSuccessListener(success -> result.setValue(new Result.Success<String>(recipe.getPhotoUrl())));

        return result;
    }


    public MutableLiveData<List<Recipe>> getRecipesByUser(String username) {
        MutableLiveData<List<Recipe>> list = new MutableLiveData<>(new ArrayList<>());
        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.RecipeEntry.AUTHOR, username)
                .orderBy(FirebaseContract.RecipeEntry.CREATION_DATE, Query.Direction.DESCENDING)
                .limit(LIMIT_QUERY)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> list.setValue(queryDocumentSnapshots.toObjects(Recipe.class)));

        return list;
    }

    public LiveData<List<Ingredient>> getIngredientsFromRecipe(String recipeId) {
        MutableLiveData<List<Ingredient>> ingredientsList = new MutableLiveData<>();

        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document(recipeId)
                .collection(FirebaseContract.RecipeEntry.IngredientEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.IngredientEntry.POSITION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> ingredientsList.setValue(queryDocumentSnapshots.toObjects(Ingredient.class)));

        return ingredientsList;
    }

    public LiveData<List<Step>> getStepsFromRecipe(String recipeId) {
        MutableLiveData<List<Step>> stepsList = new MutableLiveData<>();

        dbFirestore.collection(FirebaseContract.RecipeEntry.COLLECTION_NAME).document(recipeId)
                .collection(FirebaseContract.RecipeEntry.StepEntry.COLLECTION_NAME)
                .orderBy(FirebaseContract.RecipeEntry.StepEntry.POSITION).get()
                .addOnSuccessListener(queryDocumentSnapshots -> stepsList.setValue(queryDocumentSnapshots.toObjects(Step.class)));

        return stepsList;
    }
}
