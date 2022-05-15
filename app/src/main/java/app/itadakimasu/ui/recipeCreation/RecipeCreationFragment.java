package app.itadakimasu.ui.recipeCreation;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.itadakimasu.R;
import app.itadakimasu.databinding.FragmentRecipeCreationBinding;

public class RecipeCreationFragment extends Fragment {

    private CreationViewModel mViewModel;
    private FragmentRecipeCreationBinding binding;

    public static RecipeCreationFragment newInstance() {
        return new RecipeCreationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(CreationViewModel.class);

        binding.btAddIngredients.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_recipe_creation_to_navigation_ingredient_creation);
        });

        binding.btAddSteps.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_recipe_creation_to_navigation_step_Creation);
        });

        binding.ibGoBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}