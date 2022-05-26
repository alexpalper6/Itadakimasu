package app.itadakimasu.ui.profile;

import android.app.Application;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.AppAuthRepository;
import app.itadakimasu.data.repository.RecipesRepository;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;
import io.grpc.Context;


public class ProfileViewModel extends AndroidViewModel {
    private final RecipesRepository recipesRepository;
    private final StorageRepository storageRepository;
    private final SharedPrefRepository sharedPrefRepository;
    private final AppAuthRepository appAuthRepository;
    private MutableLiveData<List<Recipe>> recipesList;
    private String profileUsername;
    private String photoUrl;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.recipesRepository = RecipesRepository.getInstance();
        this.storageRepository = StorageRepository.getInstance();
        this.sharedPrefRepository = SharedPrefRepository.getInstance(application.getApplicationContext());
        this.appAuthRepository = AppAuthRepository.getInstance();
        this.recipesList = new MutableLiveData<>(new ArrayList<>());
    }


    public LiveData<List<Recipe>> getRecipeList() {
        return recipesList;
    }

    public LiveData<List<Recipe>> getFirstRecipes() {
       return recipesRepository.getRecipesByUser(profileUsername);
    }

    public void setRecipesList(List<Recipe> recipes) {
        this.recipesList.setValue(recipes);
    }

    public String getProfileUsername() {
        return profileUsername;
    }

    public void setProfileUsername(String profileUsername) {
        this.profileUsername = profileUsername;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void signOut() {
        appAuthRepository.signOut();
    }

    public String getAuthUsername() {
        return sharedPrefRepository.getAuthUsername();
    }

    public String getAuthUserPhotoUrl() {
        return sharedPrefRepository.getAuthUserPhotoUrl();
    }

    public boolean isListEmpty() {
        return recipesList.getValue().isEmpty();
    }

    public Recipe getRecipeFromList(int itemPosition) {
        return recipesList.getValue().get(itemPosition);
    }

    public StorageReference getImageReference(String imageUrl) {
        return storageRepository.getImageReference(imageUrl);
    }
}