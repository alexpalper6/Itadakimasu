package app.itadakimasu.ui.recipeCreation.ingredientCreation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import app.itadakimasu.R;
import app.itadakimasu.databinding.FragmentIngredientCreationBinding;
import app.itadakimasu.ui.recipeCreation.CreationViewModel;
import app.itadakimasu.utils.dialogs.AddDescriptionDialogFragment;
import app.itadakimasu.utils.dialogs.EditDescriptionDialogFragment;

/**
 * Fragment where the user can create, edit and remove ingredients from the recipe.
 */
public class IngredientCreationFragment extends Fragment {

    private CreationViewModel creationViewModel;
    private IngredientCreationAdapter adapter;
    private FragmentIngredientCreationBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIngredientCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Instantiating ViewModel, Adapter and setting the RecyclerView's layout and adapter.
        // Shares ViewModel with the navigation graph.
        NavBackStackEntry backStackEntry = NavHostFragment.findNavController(this).getBackStackEntry(R.id.creation_navigation);
        creationViewModel = new ViewModelProvider(backStackEntry).get(CreationViewModel.class);
        adapter = new IngredientCreationAdapter();
        binding.rvIngredients.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIngredients.setAdapter(adapter);


        // Sets the back button the event to go back to the previous fragment.
        binding.ibGoBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Creates a dialog where the user can write the ingredient's description.
        binding.fabAddIngredient.setOnClickListener(v -> addIngredient());

        // Shows dialog with the ingredient's description, allowing the user to edit it.
        adapter.setOnClickEditListener(this::editIngredient);

        // The remove option will remove the element that the user interacted with, using its position.
        adapter.setOnClickRemoveListener(ingredientPosition -> creationViewModel.removeIngredientAt(ingredientPosition));

        // Listeners section //

        // Observes for any change in the ingredient's list and updates the adapter.
        creationViewModel.getIngredientList().observe(getViewLifecycleOwner(), ingredientList -> adapter.submitList(new ArrayList<>(ingredientList)));

        // Receives the result of the dialog that the user uses to create an ingredient giving a description.
        // If the ingredient with given description is couldn't be created (already exists) it will show an error message.
        getParentFragmentManager().setFragmentResultListener(AddDescriptionDialogFragment.DIALOG_REQUEST, this, (requestKey, result) -> {
            String ingredientDescription = result.getString(AddDescriptionDialogFragment.DIALOG_RESULT);

            if (!creationViewModel.addIngredient(ingredientDescription)) {
                Snackbar.make(binding.getRoot(), R.string.ingredient_exists, Snackbar.LENGTH_SHORT).setAnchorView(binding.fabAddIngredient).show();
            }

        });

        // Receives the result of the dialog that the users uses to edit an ingredient's description.
        // If the ingredient to edit has different description, then it will try to change it.
        // If it can't change it because another ingredient already has the description, the system will show an error.
        getParentFragmentManager().setFragmentResultListener(EditDescriptionDialogFragment.DIALOG_REQUEST, this, ((requestKey, result) -> {
            String description = result.getString(EditDescriptionDialogFragment.DIALOG_RESULT);
            if (creationViewModel.ingredientHasDifferentDescToEdit(description)) {
                if (!creationViewModel.editIngredient(description)) {
                    Snackbar.make(binding.getRoot(), R.string.ingredient_exists, Snackbar.LENGTH_SHORT).setAnchorView(binding.fabAddIngredient).show();
                }
            }
        }));

        // Hides the button for adding ingredients when the user scrolls.
        binding.rvIngredients.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    binding.fabAddIngredient.hide();
                } else {
                    binding.fabAddIngredient.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    /**
     * Shows dialog to user that lets it enter the ingredient's description.
     */
    private void addIngredient() {
        AddDescriptionDialogFragment dialog = new AddDescriptionDialogFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(AddDescriptionDialogFragment.DIALOG_TITLE, R.string.add_ingredient_information);
        dialog.show(getParentFragmentManager(), AddDescriptionDialogFragment.TAG);
    }

    /**
     * Shows dialog with the ingredient's description, allowing the user to edit it.
     * @param ingredientPosition - the position of the ingredient in the list that is edited.
     */
    private void editIngredient(int ingredientPosition) {
        // Stores the position to edit for later use when the result of the dialog is obtained.
        creationViewModel.setItemPositionToEdit(ingredientPosition);

        EditDescriptionDialogFragment dialog = new EditDescriptionDialogFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(EditDescriptionDialogFragment.DIALOG_TITLE, R.string.ingredient_description_edit);
        bundle.putString(EditDescriptionDialogFragment.TEXT_TO_EDIT, creationViewModel.getIngredientList().getValue().get(ingredientPosition).getIngredientDescription());
        dialog.setArguments(bundle);

        dialog.show(getParentFragmentManager(), EditDescriptionDialogFragment.TAG);
    }
}