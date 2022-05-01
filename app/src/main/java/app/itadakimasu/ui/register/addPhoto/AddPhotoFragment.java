package app.itadakimasu.ui.register.addPhoto;

import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import app.itadakimasu.R;
import app.itadakimasu.databinding.FragmentAddPhotoBinding;


public class AddPhotoFragment extends Fragment {
    public static final String USERNAME_DISPLAY="app.itadakimasu.register.addPhoto.usernameDisplay";

    private AddPhotoViewModel addPhotoViewModel;
    private FragmentAddPhotoBinding binding;
    private ActivityResultLauncher<String> requestPermission;

    public static AddPhotoFragment newInstance() {
        return new AddPhotoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentAddPhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addPhotoViewModel = new ViewModelProvider(this).get(AddPhotoViewModel.class);
        addPhotoViewModel.setDisplayedUsername(getArguments().getString(USERNAME_DISPLAY));

        addPhotoViewModel.getDisplayedUsername().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String username) {
                binding.tvWelcome.setText(getString(R.string.welcome, username));
            }
        });

    }

}