package app.itadakimasu.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.databinding.ItemIngredientDisplayBinding;

/**
 * Adapter used on the ingredient's recycler view of details fragment.
 */
public class RecipeDetailsIngredientAdapter extends ListAdapter<Ingredient, DetailsIngredientViewHolder> {
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

    /**
     * Constructor to create an instance of this adapter.
     */
    public RecipeDetailsIngredientAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Creates an instance of the view holder when an item is added / created on the list of the adapter.
     * @return the view holder layout settled.
     */
    @NonNull
    @Override
    public DetailsIngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIngredientDisplayBinding binding = ItemIngredientDisplayBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false);
        return new DetailsIngredientViewHolder(binding);
    }

    /**
     * Displays the data on specified position on the list.
     * @param holder - the layout of the item, in this case the ingredient item.
     * @param position - the position where this item should appear.
     */
    @Override
    public void onBindViewHolder(@NonNull DetailsIngredientViewHolder holder, int position) {
        final Ingredient ingredient = getItem(position);
        holder.setTvIngredientDesc(ingredient.getIngredientDescription());
    }
}

/**
 * ViewHolder class that loads the layout for each item on the adapter.
 */
class DetailsIngredientViewHolder extends RecyclerView.ViewHolder {
    private final TextView tvIngredientDesc;

    /**
     * Given the binding of the inflated layout, sets the reference to the view item.
     * @param binding - the layout inflated (item_ingredient_display).
     */
    public DetailsIngredientViewHolder(ItemIngredientDisplayBinding binding) {
        super(binding.getRoot());
        this.tvIngredientDesc = binding.tvIngredientDesc;
    }

    /**
     * Sets the description on the item view.
     * @param desc - the ingredient's description.
     */
    public void setTvIngredientDesc(String desc) {
        tvIngredientDesc.setText(desc);
    }
}