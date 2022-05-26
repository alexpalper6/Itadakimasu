package app.itadakimasu.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import app.itadakimasu.R;
import app.itadakimasu.databinding.FragmentProfileBinding;
import app.itadakimasu.interfaces.OnItemClickDisplayListener;
import app.itadakimasu.ui.recipeDetails.RecipeDetailsFragment;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileRecipesAdapter adapter;
    private ProfileViewModel profileViewModel;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel notificationsViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // View model instantiation and setting username and photoUrl
        NavController navController = NavHostFragment.findNavController(this);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);


        profileViewModel.setProfileUsername(profileViewModel.getAuthUsername());
        profileViewModel.setPhotoUrl(profileViewModel.getAuthUserPhotoUrl());

        binding.tvUsername.setText(profileViewModel.getProfileUsername());
        adapter = new ProfileRecipesAdapter(requireContext());
        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipes.setAdapter(adapter);

        if (profileViewModel.isListEmpty()) {
            binding.pbProgress.setVisibility(View.VISIBLE);
            getFirstRecipes();
        }

        // Setting the image on the UI
        StorageReference reference = profileViewModel.getImageReference(profileViewModel.getPhotoUrl());
        Glide.with(requireContext()).asBitmap().load(reference).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_default_user_profile)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                            Palette p = Palette.from(resource).generate();
                            // https://www.section.io/engineering-education/extracting-colors-from-image-using-palette-api-in-android/
                            binding.getRoot().setBackgroundColor(p.getMutedColor(ContextCompat.getColor(requireContext(), R.color.primaryColor)));
                            //binding.ibSignOut.setColorFilter(p.getDominantSwatch().getBodyTextColor());
                            //binding.ibEdit.setColorFilter(p.getDominantSwatch().getBodyTextColor());
                            //binding.tvUsername.setTextColor(p.getDominantSwatch().getBodyTextColor());
                        }
                        return false;
                    }
                }).into(binding.ivUserImage);

        profileViewModel.getRecipeList().observe(getViewLifecycleOwner(), list -> adapter.submitList(list));

        binding.ibSignOut.setOnClickListener(v -> profileViewModel.signOut());

        adapter.setOnClickDisplayListener(itemPosition -> {
            Bundle result = new Bundle();
            result.putParcelable(RecipeDetailsFragment.RESULT, profileViewModel.getRecipeFromList(itemPosition));
            getParentFragmentManager().setFragmentResult(RecipeDetailsFragment.REQUEST, result);
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_profile_to_navigation_details);
        });

    }

    private void getFirstRecipes() {
        profileViewModel.getFirstRecipes().observe(getViewLifecycleOwner(), recipes -> {
            binding.pbProgress.setVisibility(View.GONE);
            if (recipes != null) {
                Log.w("Size list:", "" + recipes.size());

                profileViewModel.setRecipesList(recipes);
            } else {
                Snackbar.make(binding.getRoot(), R.string.list_retrieve_error, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}