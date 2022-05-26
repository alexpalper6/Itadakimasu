package app.itadakimasu.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import app.itadakimasu.R;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.databinding.ItemMyRecipePreviewBinding;
import app.itadakimasu.interfaces.OnItemClickDisplayListener;
import app.itadakimasu.interfaces.OnItemClickEditListener;
import app.itadakimasu.interfaces.OnItemClickRemoveListener;


public class ProfileRecipesAdapter extends ListAdapter<Recipe, ProfileRecipesViewHolder> {

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

    private final SharedPrefRepository sharedPrefRepository;
    private OnItemClickEditListener editListener;
    private OnItemClickRemoveListener removeListener;
    private OnItemClickDisplayListener displayListener;

    public ProfileRecipesAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.sharedPrefRepository = SharedPrefRepository.getInstance(context);
    }

    @NonNull
    @Override
    public ProfileRecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyRecipePreviewBinding binding = ItemMyRecipePreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProfileRecipesViewHolder(binding, editListener, removeListener, displayListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileRecipesViewHolder holder, int position) {
        final Recipe recipe = getItem(position);
        StorageReference reference = FirebaseStorage.getInstance().getReference(recipe.getPhotoUrl());
        holder.setRecipeImage(reference);
        holder.setTitle(recipe.getTitle());
        holder.setDescription(recipe.getDescription());

        if (!recipe.getAuthor().equals(sharedPrefRepository.getAuthUsername())) {
            holder.setIbMoreVisibility(View.GONE);
        }

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

    public void setOnClickDisplayListener(OnItemClickDisplayListener listener) {
        this.displayListener = listener;
    }
}

class ProfileRecipesViewHolder extends RecyclerView.ViewHolder {
    private final ImageView ivRecipeImage;
    private final TextView tvTitle;
    private final TextView tvDescription;
    private final ImageButton ibMore;


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

        binding.getRoot().setOnClickListener(v -> {
            if (displayListener != null) {
                displayListener.onItemClickDisplay(ProfileRecipesViewHolder.this.getAdapterPosition());
            }
        });
    }

    public void setRecipeImage(StorageReference imageReference) {
        Glide.with(ivRecipeImage.getContext()).load(imageReference).centerCrop().into(ivRecipeImage);
    }

    public void setTitle(String title) {
        this.tvTitle.setText(title);
    }

    public void setDescription(String description) {
        this.tvDescription.setText(description);
    }

    public void setIbMoreVisibility(int visibility) {
        this.ibMore.setVisibility(visibility);
    }

}