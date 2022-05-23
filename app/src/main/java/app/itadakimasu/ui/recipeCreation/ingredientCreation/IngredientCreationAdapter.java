package app.itadakimasu.ui.recipeCreation.ingredientCreation;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import app.itadakimasu.R;
import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.databinding.ItemIngredientBinding;
import app.itadakimasu.interfaces.OnItemClickEditListener;
import app.itadakimasu.interfaces.OnItemClickRemoveListener;

/**
 * Adapter class for ingredient creation's fragment.
 */
public class IngredientCreationAdapter extends ListAdapter<Ingredient, IngredientCreationViewHolder> {
    // DiffUtil used to calculate the difference between items on the adapter.
    public static final DiffUtil.ItemCallback<Ingredient> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {

        @Override
        public boolean areItemsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
            return oldItem.getIngredientDescription().equals(newItem.getIngredientDescription());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
            return false;
        }
    };
    // Interfaces that are used to establish actions on the "more" options from every ingredient on the list.
    private OnItemClickEditListener editListener;
    private OnItemClickRemoveListener removeListener;

    public IngredientCreationAdapter() {
        super(DIFF_CALLBACK );
    }

    /**
     * Method called when a new item is created, views are inflated using the binding and
     * the ViewHolder implementation.
     */
    @NonNull
    @Override
    public IngredientCreationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIngredientBinding binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new IngredientCreationViewHolder(binding, editListener, removeListener);
    }

    /**
     * Displays the data on specified position on the list of the recycler view.
     * @param holder - the layout of the item, in this case the ingredient item.
     * @param position - the position where this item should appear.
     */
    @Override
    public void onBindViewHolder(@NonNull IngredientCreationViewHolder holder, int position) {
        final Ingredient ingredient = getItem(position);
        holder.setIngredientDescription(ingredient.getIngredientDescription());
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
 * The ViewHolder class for the adapter of ingredients creation.
 * Describes the view and its actions that will have for each item on the list.
 */
class IngredientCreationViewHolder extends RecyclerView.ViewHolder {
    private final TextView tvIngredientDescription;

    /**
     * Giving the binding of the layout and the implementation of the interfaces, establish each
     * reference of the views and the menu for the 'more' options with each action.
     * @param binding - the inflated layout (item_ingredient)
     * @param editListener - the implementation of the edit listener
     * @param removeListener - the implementation of the remove listener
     */
    public IngredientCreationViewHolder(ItemIngredientBinding binding, OnItemClickEditListener editListener, OnItemClickRemoveListener removeListener) {
        super(binding.getRoot());
        this.tvIngredientDescription = binding.tvIngredientDesc;
        ImageButton ibMore = binding.ibMore;

        ibMore.setOnClickListener(v -> {
            // Creates a pop menu, this will display to the user the menu for edit or delete options.
            PopupMenu popupMenu = new PopupMenu(binding.getRoot().getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.more_options_menu, popupMenu.getMenu());
            // Click listener for the options.
            popupMenu.setOnMenuItemClickListener(menu -> {
                if (menu.getItemId() == R.id.options_edit) {
                    if (editListener != null) {
                        // The edit action will be performed on the item that is clicked by getting its position.
                        editListener.onItemClickEdit(IngredientCreationViewHolder.this.getAdapterPosition());
                    }
                }
                if (menu.getItemId() == R.id.options_remove) {
                    if (removeListener != null) {
                        // The remove action will be performed on the item that is clicked by getting its position.
                        removeListener.onItemClickRemove(IngredientCreationViewHolder.this.getAdapterPosition());
                    }
                }
                return true;
            });

            popupMenu.setOnDismissListener(PopupMenu::dismiss);

            popupMenu.show();
        });


    }

    // Sets the description on the TextView of the holder.
    public void setIngredientDescription(String description) {
        this.tvIngredientDescription.setText(description);
    }

}
