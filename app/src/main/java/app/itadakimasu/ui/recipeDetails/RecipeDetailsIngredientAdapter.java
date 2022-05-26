package app.itadakimasu.ui.recipeDetails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import app.itadakimasu.data.model.Ingredient;
import app.itadakimasu.databinding.ItemIngredientDisplayBinding;
import app.itadakimasu.databinding.ItemRecipePreviewBinding;

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

    public RecipeDetailsIngredientAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public DetailsIngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIngredientDisplayBinding binding = ItemIngredientDisplayBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false);
        return new DetailsIngredientViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsIngredientViewHolder holder, int position) {
        final Ingredient ingredient = getItem(position);
        holder.setTvIngredientDesc(ingredient.getIngredientDescription());
    }
}

class DetailsIngredientViewHolder extends RecyclerView.ViewHolder {
    private final TextView tvIngredientDesc;

    public DetailsIngredientViewHolder(ItemIngredientDisplayBinding binding) {
        super(binding.getRoot());
        this.tvIngredientDesc = binding.tvIngredientDesc;
    }

    public void setTvIngredientDesc(String desc) {
        tvIngredientDesc.setText(desc);
    }
}