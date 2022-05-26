package app.itadakimasu.ui.recipeDetails;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import app.itadakimasu.R;
import app.itadakimasu.data.model.Recipe;
import app.itadakimasu.databinding.FragmentRecipeDetailsBinding;

//TODO: Check that is in favourites
public class RecipeDetailsFragment extends Fragment {
    public static final String REQUEST = "app.itadakimasu.ui.recipeDetails.Request";
    public static final String RESULT = "app.itadakimasu.ui.recipeDetails.Result";

    private RecipeDetailsViewModel detailsViewModel;
    private FragmentRecipeDetailsBinding binding;

    private RecipeDetailsIngredientAdapter ingredientsAdapter;
    private RecipeDetailsStepAdapter stepsAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        detailsViewModel = new ViewModelProvider(this).get(RecipeDetailsViewModel.class);
        setupRecyclerViews();

        getParentFragmentManager().setFragmentResultListener(REQUEST, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                Recipe recipe = result.getParcelable(RESULT);
                detailsViewModel.setSelectedRecipe(recipe);
            }
        });


        detailsViewModel.getSelectedRecipe().observe(getViewLifecycleOwner(), recipe -> {
            if (recipe.getAuthor().equals(detailsViewModel.getAuthUsername())) {
                binding.cbFavourite.setVisibility(View.GONE);
            }
            StorageReference authorImageReference = detailsViewModel.getImageReference(recipe.getPhotoAuthorUrl());
            StorageReference recipeImageReference = detailsViewModel.getImageReference(recipe.getPhotoUrl());

            Glide.with(requireContext()).load(authorImageReference).circleCrop().into(binding.ivUserImage);
            Glide.with(requireContext()).load(recipeImageReference).centerCrop().into(binding.ivRecipeImage);

            binding.tvAuthor.setText(getString(R.string.created_by, recipe.getAuthor()));
            binding.tvRecipeTitle.setText(recipe.getTitle());
            binding.tvRecipeDescription.setText(recipe.getDescription());

            loadIngredientsList(recipe.getId());
            loadStepsList(recipe.getId());

        });

        detailsViewModel.getIngredientList().observe(getViewLifecycleOwner(), ingredientList -> ingredientsAdapter.submitList(ingredientList));

        detailsViewModel.getStepList().observe(getViewLifecycleOwner(), stepList -> stepsAdapter.submitList(stepList));

        binding.ibGoBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

    }

    private void loadIngredientsList(String id) {
        detailsViewModel.loadIngredientList(id).observe(getViewLifecycleOwner(), ingredientList -> {
            if (ingredientList != null) {
                detailsViewModel.setIngredientList(ingredientList);
            }
        });
    }

    private void loadStepsList(String id) {
        detailsViewModel.loadStepsList(id).observe(getViewLifecycleOwner(), stepList -> {
            if (stepList != null) {
                detailsViewModel.setStepList(stepList);
            }
        });
    }



    private void setupRecyclerViews() {
        ingredientsAdapter = new RecipeDetailsIngredientAdapter();
        stepsAdapter = new RecipeDetailsStepAdapter();

        binding.rvIngredientList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIngredientList.setAdapter(ingredientsAdapter);

        binding.rvStepList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvStepList.setAdapter(stepsAdapter);
    }

}