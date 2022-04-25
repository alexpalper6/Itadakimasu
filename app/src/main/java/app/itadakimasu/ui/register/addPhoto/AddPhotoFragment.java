package app.itadakimasu.ui.register.addPhoto;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.itadakimasu.databinding.FragmentAddPhotoBinding;


public class AddPhotoFragment extends Fragment {

    private AddPhotoViewModel addPhotoViewModel;
    private FragmentAddPhotoBinding binding;

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
    }
}