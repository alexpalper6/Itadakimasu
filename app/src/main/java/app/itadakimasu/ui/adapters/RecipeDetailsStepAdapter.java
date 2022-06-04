package app.itadakimasu.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import app.itadakimasu.data.model.Step;
import app.itadakimasu.databinding.ItemStepDisplayBinding;

/**
 * Adapter used on the step's recycler view of details fragment.
 */
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

    /**
     * Constructor to create an instance of the adapter.
     */
    public RecipeDetailsStepAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Called when the recycler view of the adapter needs a new view holder to represent an item.
     * The view holder will be an inflation of item_step_display layout.
     * @return an instance of DetailsStepViewHolder.
     */
    @NonNull
    @Override
    public DetailsStepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStepDisplayBinding binding = ItemStepDisplayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DetailsStepViewHolder(binding);
    }

    /**
     * Displays the data on specified position on the list.
     * @param holder - the layout of the item, in this case the step item.
     * @param position - the position where this item should appear.
     */
    @Override
    public void onBindViewHolder(@NonNull DetailsStepViewHolder holder, int position) {
        final Step step = getItem(position);
        holder.setTvStepPosition(Integer.toString(position + 1));
        holder.setTvStep(step.getStepDescription());
    }
}

/**
 * ViewHolder class that loads the item_step_display for each item on the adapter's list.
 */
class DetailsStepViewHolder extends RecyclerView.ViewHolder {
    private final TextView tvStepPosition;
    private final TextView tvStep;

    /**
     * Establish the references to the views.
     * @param binding - the inflated layout of item_step_display.
     */
    public DetailsStepViewHolder(ItemStepDisplayBinding binding) {
        super(binding.getRoot());
        this.tvStepPosition = binding.tvStepPosition;
        this.tvStep = binding.tvStep;
    }

    /**
     * Sets the step description.
     * @param step - the step's description from a Step object.
     */
    public void setTvStep(String step) {
        tvStep.setText(step);
    }

    /**
     * Sets the position where the step appears.
     * @param position - its position on the list.
     */
    public void setTvStepPosition(String position) {
        tvStepPosition.setText(position);
    }
}
