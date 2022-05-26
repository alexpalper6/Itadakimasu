package app.itadakimasu.ui.recipeCreation.stepCreation;

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
import app.itadakimasu.data.model.Step;
import app.itadakimasu.databinding.ItemStepBinding;
import app.itadakimasu.interfaces.OnItemClickEditListener;
import app.itadakimasu.interfaces.OnItemClickRemoveListener;

/**
 * Adapter for step's fragment RecyclerView.
 */
public class StepCreationAdapter extends ListAdapter<Step, StepCreationViewHolder> {

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

    // Implementations of the edit and remove interfaces.
    private OnItemClickEditListener editListener;
    private OnItemClickRemoveListener removeListener;

    public StepCreationAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Inflates the view that will be used on the recycler view giving the ViewHolder.
     * This will used for the items that are created on the RecyclerView from the StepCreation's fragment.
     */
    @NonNull
    @Override
    public StepCreationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStepBinding binding = ItemStepBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StepCreationViewHolder(binding, editListener, removeListener);
    }

    /**
     * Displays the data on the item given its specified position.
     * @param holder - The view holder implementation with the step item layout.
     * @param position - The position where the item is on the list.
     */
    @Override
    public void onBindViewHolder(@NonNull StepCreationViewHolder holder, int position) {
        final Step step = getItem(position);
        holder.setStepDescription(step.getStepDescription());
        holder.setStepPosition(Integer.toString(position + 1));
    }

    /**
     * Establish the implementation of the edit listener interface for this adapter.
     * @param listener - the implementation of the listener.
     */
    public void setOnClickEditListener(OnItemClickEditListener listener) {
        this.editListener = listener;
    }

    /**
     * Establish the implementation of the remove listener interface for this adapter.
     * @param listener - the implementation of the listener.
     */
    public void setOnClickRemoveListener(OnItemClickRemoveListener listener) {
        this.removeListener = listener;
    }
}

/**
 * Implementation of the view holder for the steps' recycler view adapter.
 */
class StepCreationViewHolder extends RecyclerView.ViewHolder {
    // References of the views from the layout inflated.
    private final TextView tvStepPosition;
    private final TextView tvStep;

    /**
     * Giving the binding of the layout and the implementation of the interfaces, establish each
     *  reference of the views and the menu for the 'more' options with each action.
     * @param binding - the inflated layout (item_step)
     * @param editListener - the implementation of the edit listener
     * @param removeListener - the implementation of the remove listener
     */
    public StepCreationViewHolder(ItemStepBinding binding, OnItemClickEditListener editListener, OnItemClickRemoveListener removeListener) {
        super(binding.getRoot());

        this.tvStepPosition = binding.tvStepPosition;
        this.tvStep = binding.tvStep;
        ImageButton ibMore = binding.ibMore;

        ibMore.setOnClickListener(v -> {
            // Creates pop menu with the "more options" menu.
            PopupMenu popupMenu = new PopupMenu(binding.getRoot().getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.more_options_menu, popupMenu.getMenu());
            // Establish the action for edit and remove from the menu.
            popupMenu.setOnMenuItemClickListener(menu -> {
                if (menu.getItemId() == R.id.options_edit) {
                    if (editListener != null) {
                        // The 'edit' implementation will be performed on the item selected by the user
                        // using its position.
                        editListener.onItemClickEdit(StepCreationViewHolder.this.getAdapterPosition());
                    }
                }
                if (menu.getItemId() == R.id.options_remove) {
                    if (removeListener != null) {
                        // The 'remove' implementation will be performed on the item selected by the user
                        // using its position.
                        removeListener.onItemClickRemove(StepCreationViewHolder.this.getAdapterPosition());
                    }
                }
                return true;
            });

            popupMenu.setOnDismissListener(PopupMenu::dismiss);

            popupMenu.show();
        });
    }

    // Sets the description on the TextView's holder.
    public void setStepDescription(String description) {
        this.tvStep.setText(description);
    }

    // Sets the position of the step on the holder.

    public void setStepPosition(String position) {
        this.tvStepPosition.setText(position);
    }
}

