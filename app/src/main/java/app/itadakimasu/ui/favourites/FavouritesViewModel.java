package app.itadakimasu.ui.favourites;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FavouritesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public FavouritesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Thhose are my favourite recipes");
    }

    public LiveData<String> getText() {
        return mText;
    }
}