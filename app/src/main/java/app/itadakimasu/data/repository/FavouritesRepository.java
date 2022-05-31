package app.itadakimasu.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Favourite;
import app.itadakimasu.data.model.FirebaseContract;

public class FavouritesRepository {
    private static volatile FavouritesRepository INSTANCE;
    public static int LIMIT_QUERY = 5;

    private final FirebaseFirestore dbFirestore;

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


    private FavouritesRepository() {
        this.dbFirestore = FirebaseFirestore.getInstance();
    }



    public LiveData<Result<?>> addToFavourites(String authUsername, String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();
        DocumentReference documentReference =    dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME).document();

        Favourite favourite = new Favourite(documentReference.getId(), authUsername, recipeId);

        documentReference.set(favourite)
                .addOnSuccessListener(success -> result.setValue(new Result.Success<Object>(null)))
                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));

        return result;
    }

    public LiveData<Result<?>> removeFromFavourites(String authUsername, String recipeId) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.FavouritesEntry.USERNAME, authUsername)
                .whereEqualTo(FirebaseContract.FavouritesEntry.RECIPE_ID, recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Favourite favourite = queryDocumentSnapshots.getDocuments().get(0).toObject(Favourite.class);

                    dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME).document(favourite.getId()).delete()
                            .addOnSuccessListener(success -> result.setValue(new Result.Success<Object>(null)))
                            .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));


                }).addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));



        return result;
    }

    public LiveData<Result<?>> findVafouriteRecipe(String authUsername, String id) {
        MutableLiveData<Result<?>> result = new MutableLiveData<>();

        dbFirestore.collection(FirebaseContract.FavouritesEntry.COLLECTION_NAME)
                .whereEqualTo(FirebaseContract.FavouritesEntry.USERNAME, authUsername)
                .whereEqualTo(FirebaseContract.FavouritesEntry.RECIPE_ID, id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> result.setValue(new Result.Success<Boolean>(!queryDocumentSnapshots.isEmpty())))
                .addOnFailureListener(failure -> result.setValue(new Result.Error(failure)));

        return result;
    }
}
