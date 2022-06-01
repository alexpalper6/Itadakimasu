package app.itadakimasu.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.AppAuthRepository;
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;


public class ProfileViewModel extends AndroidViewModel {
    private final RecipesRepository recipesRepository;
    private final StorageRepository storageRepository;
    private final SharedPrefRepository sharedPrefRepository;
    private final AppAuthRepository appAuthRepository;

    private final MutableLiveData<List<Recipe>> recipesList;
    private String profileUsername;
    private String photoUrl;
    // Boolean used to check if the system is fetching or removing data.
    private boolean loadingDataState;
    // Boolean used to check when the user's retrieve a list when paginating and it returns nothing.
    // This one should be checked to false when the list is reloaded.
    private boolean reachedEndPagination;


    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.recipesRepository = RecipesRepository.getInstance();
        this.storageRepository = StorageRepository.getInstance();
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.appAuthRepository = AppAuthRepository.getInstance();
        this.recipesList = new MutableLiveData<>(new ArrayList<>());
        this.loadingDataState = true;
        this.reachedEndPagination = false;
        this.profileUsername = "";
        this.photoUrl = "";
    }

    /**
     * @return the recipe list from the view model as LiveData.
     */
    public LiveData<List<Recipe>> getRecipeList() {
        return recipesList;
    }

    /**
     * Load the first recipes with given username.
     * @return the result of the method from recipes repository (list of recipes or error).
     */
    public LiveData<Result<?>> loadFirstRecipes() {
       return recipesRepository.getRecipesByUser(profileUsername);
    }

    /**
     * Used for pagination, load the next recipes using the last recipe's date on the list.
     * @return a list of the next recipes if the result is successful; error if it fails.
     */
    public LiveData<Result<?>> loadNextRecipes() {
        Date lastRecipeDate = recipesList.getValue().get(getListSize() - 1).getCreationDate();
        return recipesRepository.getNextRecipesByUser(profileUsername, lastRecipeDate);
    }

    public LiveData<Result<?>> deleteRecipe(String recipeId) {
        return recipesRepository.deleteRecipe(recipeId);
    }

    public LiveData<Result<?>> deleteRecipeImage(String recipePhotoUrl) {
        return storageRepository.deleteRecipeImage(recipePhotoUrl);
    }

    /**
     * Sets the value of recipesList.
     * @param recipes - the list of recipes.
     */
    public void setRecipesList(List<Recipe> recipes) {
        this.recipesList.setValue(recipes);
    }

    /**
     * Adds to the list the next recipes paginated.
     * @param retrievedList - the list that are retrieved.
     */
    public void addRetrievedRecipes(List<Recipe> retrievedList) {
        if (!retrievedList.isEmpty()) {
            List<Recipe> list = recipesList.getValue();
            assert list != null;
            list.addAll(retrievedList);

            recipesList.setValue(list);
        }
    }

    /**
     * Removes a Recipe from the list with given position.
     * @param recipePosition - the position of the Recipe.
     */
    public void removeRecipeAt(int recipePosition) {
        List<Recipe> list = recipesList.getValue();
        assert list != null;
        list.remove(recipePosition);

        recipesList.setValue(list);
    }

    /**
     * @return the size of the list.
     */
    public int getListSize() {
        return recipesList.getValue().size();
    }

    /**
     * @return true if the list is empty, false if not.
     */
    public boolean isListEmpty() {
        return recipesList.getValue().isEmpty();
    }

    /**
     * @param itemPosition - the position of the item to retrieve.
     * @return the recipe that is stored on the list on given position.
     */
    public Recipe getRecipeFromList(int itemPosition) {
        return recipesList.getValue().get(itemPosition);
    }

    /**
     * Gets the state of the loading data variable, used in the pagination of the recycler view,
     * to check if the user already tried to load the data or not.
     * @return true if the app is fetching data from the database; false if it's not.
     */
    public boolean isLoadingData() {
        return loadingDataState;
    }

    /**
     * Boolean used to disable pagination when the user tries to paginate data and the list doesn't
     * return any item, because the list reached its end.
     * @return true if after paginating the result of the list is empty; false if not.
     */
    public boolean reachedEndPagination() {
        return reachedEndPagination;
    }

    /**
     * Sets the pagination end state.
     * @param state - true if there is no more data to paginate, false if not.
     */
    public void setPaginationEndState(boolean state) {
        this.reachedEndPagination = state;
    }

    /**
     * Sets the state of loading data variable.
     * @param state - boolean indicating if is loading data or not.
     */
    public void setLoadingDataState(boolean state) {
        this.loadingDataState = state;
    }

    /**
     * @return the SotrageReference of the user's photo url.
     */
    public StorageReference getUserImageReference() {
        return storageRepository.getImageReference(photoUrl);
    }

    /**
     * @return the username of the profile that will be used to retrieve their data, like their recipes.
     */
    public String getProfileUsername() {
        return profileUsername;
    }

    /**
     * @return the photo url of the username.
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Sets the user's username that will be used to retrieve their data.
     * @param profileUsername - the username of the user.
     */
    public void setProfileUsername(String profileUsername) {
        this.profileUsername = profileUsername;
    }

    /**
     * Sets tue photo url of given user that will be used to show their image.
     * @param photoUrl - the photo url path.
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /**
     * @return the authenticated user's username.
     */
    public String getAuthUsername() {
        return sharedPrefRepository.getAuthUsername();
    }

    /**
     * @return the authenticated user's photo path.
     */
    public String getAuthUserPhotoUrl() {
        return sharedPrefRepository.getAuthUserPhotoUrl();
    }

    /**
     * Sign outs the authenticated user.
     */
    public void signOut() {
        appAuthRepository.signOut();
    }


}