package app.itadakimasu.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import app.itadakimasu.databinding.ItemMyRecipePreviewBinding;
import app.itadakimasu.interfaces.OnItemClickDisplayListener;
import app.itadakimasu.interfaces.OnItemClickEditListener;
import app.itadakimasu.interfaces.OnItemClickRemoveListener;

/**
 * Adapter used on the profile's RecyclerView with the item "my recipe preview"
 */
@SuppressWarnings("unchecked")
public class ProfileRecipesAdapter extends ListAdapter<Recipe, ProfileRecipesViewHolder> {

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

            return sameAuthor && sameAuthorPhoto && sameTitle && sameDesc && samePhoto;
        }
    };
    // Shared repository to remove 'more' buttons if  the username is the same as the authenticated user.
    private final SharedPrefRepository sharedPrefRepository;
    // Storage repository to load the image.
    private final StorageRepository storageRepository;
    // Lifecycle to download image's data.
    private final LifecycleOwner lifecycleOwner;
    // Interfaces from display, edit and remove  a recipe implementation.
    private OnItemClickDisplayListener displayListener;
    private OnItemClickEditListener editListener;
    private OnItemClickRemoveListener removeListener;


    /**
     * Constructor of the adapter.
     * @param context - the context of the app for loading the SharedPref repository.
     * @param viewLifecycleOwner - the lifecycler owner of the fragment
     */
    public ProfileRecipesAdapter(Context context, LifecycleOwner viewLifecycleOwner) {
        super(DIFF_CALLBACK);
        this.lifecycleOwner = viewLifecycleOwner;
        this.sharedPrefRepository = SharedPrefRepository.getInstance(context);
        this.storageRepository = StorageRepository.getInstance();
    }

    /**
     * Called when the recycler view needs to inflate the view holder when an item is created.
     */
    @NonNull
    @Override
    public ProfileRecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyRecipePreviewBinding binding = ItemMyRecipePreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProfileRecipesViewHolder(binding, editListener, removeListener, displayListener);
    }

    /**
     * Displays the data on specified position on the list of the recycler view.
     * @param holder - the layout of the item, in this case the recipe item.
     * @param position - the position where this item should appear.
     */
    @Override
    public void onBindViewHolder(@NonNull ProfileRecipesViewHolder holder, int position) {
        final Recipe recipe = getItem(position);

        // Downloads the image's data
        storageRepository.getImageUri(recipe.getPhotoUrl()).observe(lifecycleOwner, result -> {
            if (result instanceof Result.Success) {
                Uri uriImage = ((Result.Success<Uri>) result).getData();
                holder.setRecipeImage(uriImage);
            }
        });


        holder.setTitle(recipe.getTitle());
        holder.setDescription(recipe.getDescription());

        if (!recipe.getAuthor().equals(sharedPrefRepository.getAuthUsername())) {
            holder.setIbMoreVisibility(View.GONE);
        }

    }

    /**
     * Establish the display listener for this adapter.
     * @param listener - the implementation of this listener.
     */
    public void setOnClickDisplayListener(OnItemClickDisplayListener listener) {
        this.displayListener = listener;
    }

    /**
     * Establish the edit listener for this adapter.
     * @param listener - the implementation of the listener.
     */
    public void setOnClickEditListener(OnItemClickEditListener listener) {
        this.editListener = listener;
    }

    /**
     * Establish the remove listener for this adapter.
     * @param listener - the implementation of the listener.
     */
    public void setOnClickRemoveListener(OnItemClickRemoveListener listener) {
        this.removeListener = listener;
    }
}

/**
 * ViewHolder class for the adapter of profiles' recipes.
 */
class ProfileRecipesViewHolder extends RecyclerView.ViewHolder {
    private final ImageView ivRecipeImage;
    private final TextView tvTitle;
    private final TextView tvDescription;
    private final ImageButton ibMore;

    /**
     * Given a binding of a layout and the implementation of the interfaces, set all the views
     * and actions on the 'more' button and on the card view when is tapped.
     * @param binding - the inflated layout (item_my_recipe_preview).
     * @param editListener - implementation of edit listener.
     * @param removeListener - implementation of remove listener.
     * @param displayListener - implementation of display listener.
     */
    public ProfileRecipesViewHolder(ItemMyRecipePreviewBinding binding, OnItemClickEditListener editListener, OnItemClickRemoveListener removeListener,
                                    OnItemClickDisplayListener displayListener) {
        super(binding.getRoot());
        this.ivRecipeImage = binding.ivRecipeImage;
        this.tvTitle = binding.tvTitle;
        this.tvDescription = binding.tvDescription;
        this.ibMore = binding.ibMore;
        this.ibMore.setOnClickListener(v -> {
            // Creates a pop menu, this will display to the user the menu for edit or delete options.
            PopupMenu popupMenu = new PopupMenu(binding.getRoot().getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.more_options_menu, popupMenu.getMenu());
            // Click listener for the options.
            popupMenu.setOnMenuItemClickListener(menu -> {
                if (menu.getItemId() == R.id.options_edit) {
                    if (editListener != null) {
                        // The edit action will be performed on the item that is clicked by getting its position.
                        editListener.onItemClickEdit(ProfileRecipesViewHolder.this.getAdapterPosition());
                    }
                }
                if (menu.getItemId() == R.id.options_remove) {
                    if (removeListener != null) {
                        // The remove action will be performed on the item that is clicked by getting its position.
                        removeListener.onItemClickRemove(ProfileRecipesViewHolder.this.getAdapterPosition());
                    }
                }
                return true;
            });

            popupMenu.setOnDismissListener(PopupMenu::dismiss);

            popupMenu.show();
        });

        // When the card view is tapped the displayListener interface will be performed, using the
        // position of the tapped item.
        binding.getRoot().setOnClickListener(v -> {
            if (displayListener != null) {
                displayListener.onItemClickDisplay(ProfileRecipesViewHolder.this.getAdapterPosition());
            }
        });
    }

    /**
     * Sets the image using glide and the image reference on the recipe's image view.
     * @param uriImage - the downloaded image's uri.
     */
    public void setRecipeImage(Uri uriImage) {
        Glide.with(ivRecipeImage.getContext()).load(uriImage).centerCrop().into(ivRecipeImage);
    }

    /**
     * Sets the recipe's title.
     * @param title - the title of the recipe.
     */
    public void setTitle(String title) {
        this.tvTitle.setText(title);
    }

    /**
     * Sets the recipe's description.
     * @param description - the recipe's description.
     */
    public void setDescription(String description) {
        this.tvDescription.setText(description);
    }

    /**
     * Sets the 'more' button visibility.
     * @param visibility View.GONE for invisible, View.VISIBLE for visible.
     */
    public void setIbMoreVisibility(int visibility) {
        this.ibMore.setVisibility(visibility);
    }

}