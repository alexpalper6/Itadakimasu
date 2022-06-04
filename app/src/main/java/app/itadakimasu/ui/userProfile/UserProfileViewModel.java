package app.itadakimasu.ui.userProfile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.data.repository.StorageRepository;

/**
 * View model for different user profiles.
 */
public class UserProfileViewModel extends ViewModel {
    // Repositories used to obtain the recipes data and the images.
    private final RecipesRepository recipesRepository;
    private final StorageRepository storageRepository;
    // List of recipes.
    private final MutableLiveData<List<Recipe>> recipeList;
    // The user's username and their profile picture.
    private final MutableLiveData<String> profileUsername;
    private final MutableLiveData<String> photoUrl;
    // Data states used to tell if the data is loading and if there is no more data to fetch.
    private boolean loadingDataState;
    private boolean reachedEndPagination;

    public UserProfileViewModel() {
        this.recipesRepository = RecipesRepository.getInstance();
        this.storageRepository = StorageRepository.getInstance();
        this.recipeList = new MutableLiveData<>(new ArrayList<>());
        this.profileUsername = new MutableLiveData<>();
        this.photoUrl = new MutableLiveData<>();
        this.loadingDataState = false;
        this.reachedEndPagination = false;
    }

    /**
     * @return the recipe list from the view model as LiveData.
     */
    public LiveData<List<Recipe>> getRecipeList() {
        return recipeList;
    }

    /**
     * @return the username as LiveData.
     */
    public LiveData<String> getProfileUsername() {
        return profileUsername;
    }

    /**
     * @return the user's photo url as LiveData.
     */
    public LiveData<String> getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Load the first recipes from the user.
     * @return the result of the method from recipes repository (list of recipes or error).
     */
    public LiveData<Result<?>> loadFirstRecipes() {
        return recipesRepository.getRecipesByUser(profileUsername.getValue());
    }

    /**
     * Used for pagination, load the next recipes using the last recipe's date on the list.
     * @return a list of the next recipes if the result is successful; error if it fails.
     */
    public LiveData<Result<?>> loadNextRecipes() {
        Date lastRecipeDate = recipeList.getValue().get(getListSize() - 1).getCreationDate();
        return recipesRepository.getNextRecipesByUser(profileUsername.getValue(), lastRecipeDate);
    }

    /**
     * @param userImageUrl - the user image's path to download the data.
     * @return image's data as uri or result error if it fails.
     */
    public LiveData<Result<?>> downloadUserImage(String userImageUrl) {
        return storageRepository.getImageUri(userImageUrl);
    }


    /**
     * Sets the value of recipesList.
     * @param recipes - the list of recipes.
     */
    public void setRecipesList(List<Recipe> recipes) {
        this.recipeList.setValue(recipes);
    }

    /**
     * Adds to the list the next recipes paginated.
     * @param retrievedList - the list that are retrieved.
     */
    public void addRetrievedRecipes(List<Recipe> retrievedList) {
        if (!retrievedList.isEmpty()) {
            List<Recipe> list = recipeList.getValue();
            assert list != null;
            list.addAll(retrievedList);

            recipeList.setValue(list);
        }
    }

    /**
     * Sets the username, to get the user's profile.
     * @param username - the user's name to whom we will se the recipes.
     */
    public void setProfileUsername(String username) {
        profileUsername.setValue(username);
    }

    /**
     * Sets the user's url path to get their image.
     * @param userPhotoUrl - the user's photo url.
     */
    public void setPhotoUrl(String userPhotoUrl) {
        photoUrl.setValue(userPhotoUrl);
    }

    /**
     * @return the size of the list.
     */
    public int getListSize() {
        return recipeList.getValue().size();
    }

    /**
     * @return true if the list is empty, false if not.
     */
    public boolean isListEmpty() {
        return recipeList.getValue().isEmpty();
    }

    /**
     * @param itemPosition - the position of the item to retrieve.
     * @return the recipe that is stored on the list on given position.
     */
    public Recipe getRecipeFromList(int itemPosition) {
        return recipeList.getValue().get(itemPosition);
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
     * @return the StorageReference of the user's photo url.
     */
    public StorageReference getUserImageReference(String photoUrl) {
        return storageRepository.getImageReference(photoUrl);
    }

}