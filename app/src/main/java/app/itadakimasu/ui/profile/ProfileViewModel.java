package app.itadakimasu.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.AppAuthRepository;
import app.itadakimasu.data.repository.RecipesRepository;


public class ProfileViewModel extends ViewModel {
    private final RecipesRepository recipesRepository;
    private final AppAuthRepository appAuthRepository;
    private MutableLiveData<List<Recipe>> recipesList;
    private String username;
    private String photoUrl;

    public ProfileViewModel() {
        this.recipesRepository = RecipesRepository.getInstance();
        this.appAuthRepository = AppAuthRepository.getInstance();
        this.recipesList = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<Recipe>> getRecipeList() {
        return recipesList;
    }

    public void setFirstRecipes() {
        recipesList = recipesRepository.getRecipesByUser(username);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}