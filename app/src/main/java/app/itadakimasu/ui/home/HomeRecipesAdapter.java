package app.itadakimasu.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.StorageReference;

import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;
import app.itadakimasu.databinding.ItemRecipePreviewBinding;
import app.itadakimasu.interfaces.OnItemClickAddFavListener;
import app.itadakimasu.interfaces.OnItemClickDisplayListener;
import app.itadakimasu.interfaces.OnItemClickShowProfileListener;

/**
 * Adapter used on the home fragment's recycler view.
 */
public class HomeRecipesAdapter extends ListAdapter<Recipe, HomeRecipesViewHolder> {

    // Diff callback to check the difference between recipes.
    public static final DiffUtil.ItemCallback<Recipe> DIFF_CALLBACK = new DiffUtil.ItemCallback<Recipe>() {
        @Override
        public boolean areItemsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
            boolean  sameAuthor = oldItem.getAuthor().equals(newItem.getAuthor());
            boolean sameAuthorPhoto = oldItem.getPhotoAuthorUrl().equals(newItem.getPhotoAuthorUrl());

            boolean sameTitle = oldItem.getTitle().equals(newItem.getTitle());
            boolean sameDesc = oldItem.getDescription().equals(newItem.getDescription());
            boolean samePhoto = oldItem.getPhotoUrl().equals(newItem.getPhotoUrl());

            return sameAuthor && sameAuthorPhoto && sameTitle && sameDesc && samePhoto;
        }
    };

    // Used to get the authenticated user's  username.
    private final SharedPrefRepository sharedPrefRepository;
    // Used to load the images on their respective view using storage references.
    private final StorageRepository storageRepository;

    private OnItemClickDisplayListener displayListener;
    private OnItemClickAddFavListener favListener;
    private OnItemClickShowProfileListener showProfileListener;

    protected HomeRecipesAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.sharedPrefRepository = SharedPrefRepository.getInstance(context);
        this.storageRepository = StorageRepository.getInstance();
    }

    /**
     * Inflates and creates a recycled layout for a recipe's preview.
     */
    @NonNull
    @Override
    public HomeRecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipePreviewBinding binding = ItemRecipePreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HomeRecipesViewHolder(binding, displayListener, favListener, showProfileListener);
    }

    /**
     * Sets data on the recycler view given a recipe from the list.
     * @param position - the position where the recipe is on the list.
     */
    @Override
    public void onBindViewHolder(@NonNull HomeRecipesViewHolder holder, int position) {
        final Recipe recipe = getItem(position);

        if (sharedPrefRepository.getAuthUsername().equals(recipe.getAuthor())) {
            holder.setCheckVisibility(View.GONE);
        }

        StorageReference recipeImageReference = storageRepository.getImageReference(recipe.getPhotoUrl());
        holder.setRecipeImage(recipeImageReference);

        StorageReference userImageReference = storageRepository.getImageReference(recipe.getPhotoAuthorUrl());
        holder.setUserImage(userImageReference);

        holder.setCheckedFavourite(recipe.isFavourite());
        holder.setTitle(recipe.getTitle());
        holder.setUsername(recipe.getAuthor());
        holder.setDescription(recipe.getDescription());
    }

    /**
     * Establish the click listener for this adapter.
     * @param listener - the implementation of this listener.
     */
    public void setOnClickDisplayListener(OnItemClickDisplayListener listener) {
        this.displayListener = listener;
    }

    public void setOnClickShowProfileListener(OnItemClickShowProfileListener listener) {
        this.showProfileListener = listener;
    }

    /**
     * Establish the click listener for this adapter, to add to favourites.
     * @param listener - the implementation of this listener.
     */
    public void setOnClickAddFavListener(OnItemClickAddFavListener listener) {
        this.favListener = listener;
    }
}

/**
 * Holder that describes the recipe preview views and establish the listeners.
 * This layout has every part of the ui for the recipes preview cards.
 */
class HomeRecipesViewHolder extends RecyclerView.ViewHolder {
    private final ImageView ivRecipeImage;
    private final CheckBox cbFavourite;
    private final ImageView ivUserImage;
    private final TextView tvTitle;
    private final TextView tvUsername;
    private final TextView tvDescription;

    public HomeRecipesViewHolder(ItemRecipePreviewBinding binding, OnItemClickDisplayListener displayListener, OnItemClickAddFavListener favListener,
                                 OnItemClickShowProfileListener showProfileListener) {
        super(binding.getRoot());
        this.ivRecipeImage = binding.ivRecipeImage;
        this.cbFavourite = binding.cbFavourite;
        this.ivUserImage = binding.ivUserImage;
        this.tvTitle = binding.tvTitle;
        this.tvUsername = binding.tvUsername;
        this.tvDescription = binding.tvDescription;

        // Sets the method of the interface that will be implemented on the fragment to add or remove the recipe from
        // favourites
        this.cbFavourite.setOnClickListener(v -> {
            if (favListener != null) {
                favListener.onItemAddFavListener(HomeRecipesViewHolder.this.getAdapterPosition());
            }
        });

        this.ivUserImage.setOnClickListener(v -> {
            if (showProfileListener != null) {
                showProfileListener.onItemShowProfile(HomeRecipesViewHolder.this.getAdapterPosition());
            }
        });
        // Used to implement the method of the interface on the fragment to show the detailed data
        // of the selected recipe.
        binding.getRoot().setOnClickListener(v -> {
            if (displayListener != null) {
                displayListener.onItemClickDisplay(HomeRecipesViewHolder.this.getAdapterPosition());
            }
        });
    }

    /**
     * Loads the recipe's image.
     * @param storageReference - the reference where the recipe's image path is stored.
     */
    public void setRecipeImage(StorageReference storageReference) {
        Glide.with(ivRecipeImage.getContext()).load(storageReference).centerCrop().into(ivRecipeImage);
    }

    /**
     * Sets the sate of the recipe, if its a a favourite recipe of the authenticated user or not.
     * @param isFavourite - true if is marked as favourite; fasle if not.
     */
    public void setCheckedFavourite(boolean isFavourite) {
        cbFavourite.setChecked(isFavourite);
    }

    /**
     * Loads the user that uploaded the recipe image.
     * @param storageReference - the reference where the user's photo path is stored.
     */
    public void setUserImage(StorageReference storageReference) {
        Glide.with(ivUserImage.getContext()).load(storageReference).circleCrop().into(ivUserImage);
    }

    /**
     * Sets the title for the recipe.
     * @param title - the title of the recipe.
     */
    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    /**
     * Sets the ui view of the user that created the recipe.
     * @param username - the user's username.
     */
    public void setUsername(String username) {
        tvUsername.setText(username);
    }

    /**
     * Sets the text on the ui view of the recipe's description.
     * @param description - the recipe's description.
     */
    public void setDescription(String description) {
        tvDescription.setText(description);
    }

    /**
     * Changes visibility of the check button, in case that the recipe is from the authenticated user.
     * @param visibility - GONE if the recipe is created by the authenticated user; VISIBLE if not.
     */
    public void setCheckVisibility(int visibility) {
        cbFavourite.setVisibility(visibility);
    }
}