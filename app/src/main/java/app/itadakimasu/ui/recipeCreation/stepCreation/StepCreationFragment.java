package app.itadakimasu.ui.recipeCreation.stepCreation;

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
import app.itadakimasu.databinding.FragmentStepCreationBinding;
import app.itadakimasu.ui.recipeCreation.CreationViewModel;
import app.itadakimasu.utils.dialogs.AddDescriptionDialogFragment;
import app.itadakimasu.utils.dialogs.EditDescriptionDialogFragment;

/**
 * Fragment where the user can create, edit and remove steps from the recipe.
 */
public class StepCreationFragment extends Fragment {
    private CreationViewModel creationViewModel;
    private StepCreationAdapter adapter;
    private FragmentStepCreationBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStepCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Instantiation and configuration of ViewModel, Adapter and RecyclerViewModel.
        // Shares ViewModel with the navigation graph.
        NavBackStackEntry backStackEntry = NavHostFragment.findNavController(this).getBackStackEntry(R.id.creation_navigation);
        creationViewModel = new ViewModelProvider(backStackEntry).get(CreationViewModel.class);
        adapter = new StepCreationAdapter();
        binding.ibGoBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.rvSteps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSteps.setAdapter(adapter);



        // Sets the back button the event to go back to the previous fragment.
        binding.ibGoBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Shows dialog where the user can input the step's description.
        binding.fabAddStep.setOnClickListener(v -> addStep());
        // Shows dialog with the step's description, allowing the user to edit it.
        adapter.setOnClickEditListener(this::editStep);

        // Removes the step selected when the user selects the 'Remove' option
        adapter.setOnClickRemoveListener(itemPosition -> creationViewModel.removeStepAt(itemPosition));

        // Listeners section //

        // Observes for changes in the list of steps and updates the adapter.
        creationViewModel.getStepList().observe(getViewLifecycleOwner(), stepList -> adapter.submitList(new ArrayList<>(stepList)));

        // Receives the result of the dialog that the user uses to create a step giving a description.
        // If the step with given description is couldn't be created (already exists) the user will receive an error message.
        getParentFragmentManager().setFragmentResultListener(AddDescriptionDialogFragment.DIALOG_REQUEST, this, ((requestKey, result) -> {
            String stepDescription = result.getString(AddDescriptionDialogFragment.DIALOG_RESULT);

            if (!creationViewModel.addStep(stepDescription)) {
                Snackbar.make(binding.getRoot(), R.string.step_exists, Snackbar.LENGTH_SHORT).setAnchorView(binding.fabAddStep).show();

            }
        }));

        // Receives the result of the dialog that the users uses to edit a step's description.
        // If the step to edit has different description, then it will try to change it.
        // If it can't change it because another step already has the description, the user will receive an error message.
        getParentFragmentManager().setFragmentResultListener(EditDescriptionDialogFragment.DIALOG_REQUEST, this, ((requestKey, result) -> {
            String description = result.getString(EditDescriptionDialogFragment.DIALOG_RESULT);

            if (creationViewModel.stepHasDifferentDescToEdit(description)) {
                if (!creationViewModel.editStep(description)) {
                    Snackbar.make(binding.getRoot(), R.string.ingredient_exists, Snackbar.LENGTH_SHORT).setAnchorView(binding.fabAddStep).show();
                }
            }
        }));


        // Hides the button for adding steps when the user scrolls.
        binding.rvSteps.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    binding.fabAddStep.hide();
                } else {
                    binding.fabAddStep.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    /**
     * Shows dialog that lets the user to set a step's description and add it to the list.
     */
    private void addStep() {
        AddDescriptionDialogFragment dialog = new AddDescriptionDialogFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(AddDescriptionDialogFragment.DIALOG_TITLE, R.string.add_step_information);
        dialog.show(getParentFragmentManager(), AddDescriptionDialogFragment.TAG);
    }

    /**
     * Dialogs that prompts with a step's data in order to edit it.
     * @param stepPosition - the step from the list that will be edited.
     */
    private void editStep(int stepPosition) {
        // Sets the position of the edited item for future usage when the data is returned by the dialog.
        creationViewModel.setItemPositionToEdit(stepPosition);

        EditDescriptionDialogFragment dialog = new EditDescriptionDialogFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(EditDescriptionDialogFragment.DIALOG_TITLE, R.string.step_description_edit);
        bundle.putString(EditDescriptionDialogFragment.TEXT_TO_EDIT, creationViewModel.getStepList().getValue().get(stepPosition).getStepDescription());
        dialog.setArguments(bundle);

        dialog.show(getParentFragmentManager(), EditDescriptionDialogFragment.TAG);
    }
}