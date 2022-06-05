package app.itadakimasu.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;
import app.itadakimasu.databinding.ItemRecipePreviewBinding;
import app.itadakimasu.interfaces.OnItemClickAddFavListener;
import app.itadakimasu.interfaces.OnItemClickDisplayListener;
import app.itadakimasu.interfaces.OnItemClickShowProfileListener;

/**
 * Adapter used on the home fragment and favourites fragment's recycler view.
 */
@SuppressWarnings("unchecked")
public class RecipePreviewAdapter extends ListAdapter<Recipe, RecipePreviewViewHolder> {

    // Diff callback to check the difference between recipes.
    public static final DiffUtil.ItemCallback<Recipe> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
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
            boolean isSameFav = oldItem.isFavourite().equals(newItem.isFavourite());
            return sameAuthor && sameAuthorPhoto && sameTitle && sameDesc && samePhoto && isSameFav;
        }
    };

    // Used to get the authenticated user's  username.
    private final SharedPrefRepository sharedPrefRepository;
    // Used to load the images on their respective view using storage references.
    private final StorageRepository storageRepository;
    // Lifecycle to download images' data.
    private final LifecycleOwner lifecycleOwner;

    // Implementations of interfaces in order to display, add to favourites a recipe and to visit
    // the author's profile.
    private OnItemClickDisplayListener displayListener;
    private OnItemClickAddFavListener favListener;
    private OnItemClickShowProfileListener showProfileListener;

    public RecipePreviewAdapter(Context context, LifecycleOwner viewLifecycleOwner) {
        super(DIFF_CALLBACK);
        this.lifecycleOwner = viewLifecycleOwner;
        this.sharedPrefRepository = SharedPrefRepository.getInstance(context);
        this.storageRepository = StorageRepository.getInstance();
    }

    /**
     * Inflates and creates a recycled layout for a recipe's preview.
     */
    @NonNull
    @Override
    public RecipePreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipePreviewBinding binding = ItemRecipePreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecipePreviewViewHolder(binding, displayListener, favListener, showProfileListener);
    }

    /**
     * Sets data on the recycler view given a recipe from the list.
     * @param position - the position where the recipe is on the list.
     */
    @Override
    public void onBindViewHolder(@NonNull RecipePreviewViewHolder holder, int position) {
        final Recipe recipe = getItem(position);

        if (sharedPrefRepository.getAuthUsername().equals(recipe.getAuthor())) {
            holder.setCheckVisibility(View.GONE);
        }

        // Downloads and sets the recipe image.
        storageRepository.getImageUri(recipe.getPhotoUrl()).observe(lifecycleOwner, result -> {
            if (result instanceof Result.Success) {
                Uri uriImage = ((Result.Success<Uri>) result).getData();
                holder.setRecipeImage(uriImage);
            }
        });

        // Downloads and sets the author's image.
        storageRepository.getImageUri(recipe.getPhotoAuthorUrl()).observe(lifecycleOwner, result -> {
            if (result instanceof Result.Success) {
                Uri uriImage = ((Result.Success<Uri>) result).getData();
                holder.setUserImage(uriImage);
            }
        });

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
class RecipePreviewViewHolder extends RecyclerView.ViewHolder {
    private final ImageView ivRecipeImage;
    private final CheckBox cbFavourite;
    private final ImageView ivUserImage;
    private final TextView tvTitle;
    private final TextView tvUsername;
    private final TextView tvDescription;

    /**
     *  Given an inflation of the layout and the implementation of the interfaces, set all the views
     *  and actions on their respective views.
     * @param binding - the inflation of item_recipe_preview.
     * @param displayListener - implementation of OnItemClickDisplayListener.
     * @param favListener - implementation of OnItemClickAddFavListener.
     * @param showProfileListener - implementation of OnItemClickDisplayListener.
     */
    public RecipePreviewViewHolder(ItemRecipePreviewBinding binding, OnItemClickDisplayListener displayListener, OnItemClickAddFavListener favListener,
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
                favListener.onItemAddFavListener(RecipePreviewViewHolder.this.getAdapterPosition());
            }
        });

        this.ivUserImage.setOnClickListener(v -> {
            if (showProfileListener != null) {
                showProfileListener.onItemShowProfile(RecipePreviewViewHolder.this.getAdapterPosition());
            }
        });
        // Used to implement the method of the interface on the fragment to show the detailed data
        // of the selected recipe.
        binding.getRoot().setOnClickListener(v -> {
            if (displayListener != null) {
                displayListener.onItemClickDisplay(RecipePreviewViewHolder.this.getAdapterPosition());
            }
        });

        binding.getRoot().setOnLongClickListener(v -> {
            if (favListener != null) {
                favListener.onItemAddFavListener(RecipePreviewViewHolder.this.getAdapterPosition());
                return true;
            }
           return false;
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
     * Loads the recipe's image.
     * @param uriImage - the downloaded image's uri.
     */
    public void setRecipeImage(Uri uriImage) {
        Glide.with(ivRecipeImage.getContext()).load(uriImage).error(R.drawable.ic_baseline_image_not_supported_24).centerCrop().into(ivRecipeImage);
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
     * Loads the user that uploaded the recipe image.
     * @param uriImage - the downloaded image's uri.
     */
    public void setUserImage(Uri uriImage) {
        Glide.with(ivUserImage.getContext()).load(uriImage).error(R.drawable.ic_default_user_profile).circleCrop().into(ivUserImage);
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