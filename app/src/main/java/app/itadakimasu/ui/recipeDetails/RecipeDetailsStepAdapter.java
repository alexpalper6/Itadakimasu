package app.itadakimasu.ui.recipeDetails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import app.itadakimasu.data.model.Step;
import app.itadakimasu.databinding.ItemStepDisplayBinding;

public class RecipeDetailsStepAdapter extends ListAdapter<Step, DetailsStepViewHolder> {
    // DiffUtil implementation to calculate the difference between items.
    public static final DiffUtil.ItemCallback<Step> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Step oldItem, @NonNull Step newItem) {
            return oldItem.getStepDescription().equals(newItem.getStepDescription());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Step oldItem, @NonNull Step newItem) {
            return false;
        }
    };


    public RecipeDetailsStepAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public DetailsStepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStepDisplayBinding binding = ItemStepDisplayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DetailsStepViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsStepViewHolder holder, int position) {
        final Step step = getItem(position);
        holder.setTvStepPosition(Integer.toString(position + 1));
        holder.setTvStep(step.getStepDescription());
    }
}

class DetailsStepViewHolder extends RecyclerView.ViewHolder {
    private final TextView tvStepPosition;
    private final TextView tvStep;

    public DetailsStepViewHolder(ItemStepDisplayBinding binding) {
        super(binding.getRoot());
        this.tvStepPosition = binding.tvStepPosition;
        this.tvStep = binding.tvStep;
    }

    public void setTvStep(String step) {
        tvStep.setText(step);
    }

    public void setTvStepPosition(String position) {
        tvStepPosition.setText(position);
    }
}
