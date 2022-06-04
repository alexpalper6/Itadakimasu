package app.itadakimasu.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.List;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Favourite;
import app.itadakimasu.data.model.FirebaseContract;

/**
 * Repository that creates, retrieves and delete favourites documents from firebase's database.
 */
public class FavouritesRepository {
    private static final String TAG = "FavouritesRepository";
    // Repository's singleton
    private static volatile FavouritesRepository INSTANCE;
    // Limit of how many documents will be fetched for each query.
    public static int LIMIT_QUERY = 5;
    // Data source of firebase firestore database.
    private final FirebaseFirestore dbFirestore;

    /**
     * @return favourites repository singleton.
     */
    public static FavouritesRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (FavouritesRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FavouritesRepository();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Sets the firebase firestore data source instance.
     */
    private FavouritesRepository() {
        this.dbFirestore = FirebaseFirestore.getInstance();
    }

    /**
     * Given the username who added a recipe to favourites, get their first newest documents, ordered
     * by date.
     * @param authUsername - the user who added the recipes as favourites.
     * @return result success with the list of favourites entries; result error if it fails.
     */
    public LiveData<Result<?>> getNewestFavouritesByUser(String authUsername) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "getNewestFavouritesByUser: obtaining newest favourites entries");
        
        dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.FavouritesEntry.USERNAME, authUsername)
                .orderBy(FirebaseContract.FavouritesEntry.ADDITION_DATE, Query.Direction.DESCENDING)
                .limit(LIMIT_QUERY)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Favourite>>(queryDocumentSnapshots.toObjects(Favourite.class))))
                .addOnFailureListener(failure -> {
                    result.setValue(new Result.Error(failure));
                    Log.e(TAG, "getNewestFavouritesByUser: error obtaining favourites entries.", failure);
                });

        return result;
    }

    /**
     * Given the username who added a recipe to favourites and the last favourite entry addition date,
     * get their next documents, paginating using the last favourite entry.
     * @param authUsername - the user who added the recipes as favourites.
     * @param lastFavDate - the last favourite entry date from the list that the user actually has.
     * @return result success with the list of favourites entries; result error if it fails.
     */
    public LiveData<Result<?>> getNextFavouritesByUser(String authUsername, Date lastFavDate) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "getNextFavouritesByUser: obtaining next recipes");
        
        dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.FavouritesEntry.USERNAME, authUsername)
                .orderBy(FirebaseContract.FavouritesEntry.ADDITION_DATE, Query.Direction.DESCENDING)
                .startAfter(lastFavDate)
                .limit(LIMIT_QUERY)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<List<Favourite>>(queryDocumentSnapshots.toObjects(Favourite.class))))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "getNextFavouritesByUser: error obtaining next recipes", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;

    }

    /**
     * Adds to the favourites collections an entry with the username who added it and the recipe's id
     * that it's marked as favourite.
     * An id and the addition date will be assigned by a document reference and firebase.
     *
     * @param authUsername - the user who added the recipe's to favourite.
     * @param recipeId - the recipe's id reference that was added to favourites.
     * @return result success if the uploading is successful; error if not.
     */
    public LiveData<Result<?>> addToFavourites(String authUsername, String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        
        // Before being added to the favourites collection, it will check for a existent document
        // with the same data, in case that there is a problem on the client side, so it won't be
        // added twice.
        dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.FavouritesEntry.USERNAME, authUsername)
                .whereEqualTo(FirebaseContract.FavouritesEntry.RECIPE_ID, recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // If there is no entry, then the document will be uploaded.
                    Log.i(TAG, "addToFavourites: checking if favourite entry exists");
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.i(TAG, "addToFavourites: favourite entry doesn't exist, creating");
                        DocumentReference documentReference = dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME).document();

                        Favourite favourite = new Favourite(documentReference.getId(), authUsername, recipeId);

                        documentReference.set(favourite)
                                .addOnSuccessListener(success -> result.setValue(new Result.Success<Object>(null)))
                                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));
                    } else {
                        result.setValue(new Result.Success<Object>(null));
                    }

                }).addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));

        return result;
    }

    /**
     * Removes the favourite document with given username who added the recipe, and the recipe's id.
     *
     * @param authUsername - the user who added the recipe to favourite.
     * @param recipeId - the recipe's id.
     * @return result success if it's deleted; error if not.
     */
    public LiveData<Result<?>> removeFromFavourites(String authUsername, String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "removeFromFavourites: obtaining favourite entry to remove");
        // First, for being able to deleted it's necessary to obtain the document reference with the id.
        dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.FavouritesEntry.USERNAME, authUsername)
                .whereEqualTo(FirebaseContract.FavouritesEntry.RECIPE_ID, recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Favourite favourite = queryDocumentSnapshots.getDocuments().get(0).toObject(Favourite.class);

                    assert favourite != null;
                    dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME).document(favourite.getId()).delete()
                            .addOnSuccessListener(success -> {
                                Log.i(TAG, "removeFromFavourites: favourite entry removed");
                                result.setValue(new Result.Success<Object>(null));
                            })
                            .addOnFailureListener(failure -> {
                                Log.e(TAG, "removeFromFavourites: error removing recipe", failure);
                                result.setValue(new Result.Error(failure));
                            });


                }).addOnFailureListener(failure -> {
                    Log.e(TAG, "removeFromFavourites: couldn't retrieve favourite entry", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }

    /**
     * Remove every document that contains given recipe's id.
     * @param recipeId - the recipe id to obtain the documents.
     * @return Result.Error if something goes wrong.
     */
    public LiveData<Result<?>> removeEveryEntryWithRecipe(String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "removeEveryEntryWithRecipe: obtaining favourites entries");
        dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.FavouritesEntry.RECIPE_ID, recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    result.setValue(new Result.Success<Object>(null));
                    Log.i(TAG, "removeEveryEntryWithRecipe: removing favourites entries");
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete().addOnFailureListener(failure ->
                                Log.e(TAG, "removeEveryEntryWithRecipe: error removin entrie", failure));
                    }
                }).addOnFailureListener(failure -> {
                    Log.e(TAG, "removeEveryEntryWithRecipe: error obtaining entries", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }

    /**
     * Finds a favourite recipe with given user and recipe's id.
     * Used to check if a recipe is a favourite one from the authenticated user.
     * @param authUsername - the authenticated username.
     * @param recipeId - the recipe's id.
     * @return result success with the result if the document is found or not; error if something wrong happens.
     */
    public LiveData<Result<?>> findFavouriteRecipe(String authUsername, String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        Log.i(TAG, "findFavouriteRecipe: obtaining favourite entry");
        dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.FavouritesEntry.USERNAME, authUsername)
                .whereEqualTo(FirebaseContract.FavouritesEntry.RECIPE_ID, recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<Boolean>(!queryDocumentSnapshots.isEmpty())))
                .addOnFailureListener(failure -> {
                    Log.e(TAG, "findFavouriteRecipe: error obtaining the entry", failure);
                    result.setValue(new Result.Error(failure));
                });

        return result;
    }
}
