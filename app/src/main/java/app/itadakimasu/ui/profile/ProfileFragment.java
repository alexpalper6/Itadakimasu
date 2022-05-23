package app.itadakimasu.ui.profile;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.circularreveal.CircularRevealHelper;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import app.itadakimasu.R;
import app.itadakimasu.data.model.FirebaseContract;
import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.data.repository.StorageRepository;
import app.itadakimasu.databinding.FragmentProfileBinding;
import app.itadakimasu.utils.dialogs.SelectMediaDialogFragment;
import app.itadakimasu.utils.ImageCompressorUtils;
import app.itadakimasu.utils.ImageCropUtils;

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
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        profileViewModel.setUsername(sharedPreferences.getString(SharedPrefRepository.SAVED_USERNAME_KEY, ""));
        profileViewModel.setPhotoUrl(sharedPreferences.getString(SharedPrefRepository.SAVED_PHOTO_URL_KEY, ""));
        profileViewModel.setFirstRecipes();

        binding.tvUsername.setText(profileViewModel.getUsername());

        adapter = new ProfileRecipesAdapter(requireActivity());
        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecipes.setAdapter(adapter);




        // Setting the image on the UI
        StorageReference reference = FirebaseStorage.getInstance().getReference(profileViewModel.getPhotoUrl());
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

    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}