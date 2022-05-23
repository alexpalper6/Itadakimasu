package app.itadakimasu.ui.auth.login;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import app.itadakimasu.data.repository.SharedPrefRepository;
import app.itadakimasu.databinding.FragmentLoginBinding;

import app.itadakimasu.R;

/**
 * Fragment that lets the user to login and enter the app.
 */
public class LoginFragment extends Fragment implements FirebaseAuth.AuthStateListener {
    // The view model that survives Android configuration change and saves the state.
    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        FirebaseAuth.getInstance().addAuthStateListener(this);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        // Observes for the form state, when the user enters data to the email and password fields.
        // If data is valid, the login button will be enabled; else, it will be disabled.
        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), loginFormState -> {
            // Checks for the user email error message and shows it to the user.
            if (loginFormState.getUserEmailError() != null) {
                binding.tilEmail.setError(getString(loginFormState.getUserEmailError()));
            } else {
                binding.tilEmail.setError(null);
            }

            binding.btLogin.setEnabled(loginFormState.isDataValid());
        });

        // Observes for the logging error result and shows to the user the error message via Snackbar.
        loginViewModel.getLoginErrorResult().observe(getViewLifecycleOwner(), loginResultState -> {
            binding.loading.setVisibility(View.GONE);
            Snackbar.make(binding.getRoot()
                    , loginResultState.getError()
                    , BaseTransientBottomBar.LENGTH_LONG).show();
        });

        // When clicking on register button, the user is sent to the register fragment.
        binding.btRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_login_to_navigation_register)
        );

        // Performs the login with the fields' data.
        binding.btLogin.setOnClickListener(v -> login(binding.etEmail.getText().toString(), binding.etPassword.getText().toString()));

        // Text watcher that observes the data changes on the fields.
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(binding.etEmail.getText().toString(), binding.etPassword.getText().toString());
            }
        };

        // Sets the text changed listener with the text watcher.
        binding.etEmail.addTextChangedListener(afterTextChangedListener);
        binding.etPassword.addTextChangedListener(afterTextChangedListener);

    }

    /**
     * Logins the user, and observes for the error, if the error is not null, then it will show
     * the error message.
     * @param userEmail - the user's email.
     * @param password - the user's password.
     */
    private void login(String userEmail, String password) {
        binding.loading.setVisibility(View.VISIBLE);
        loginViewModel.login(userEmail, password).observe(getViewLifecycleOwner(), error -> {
            if (error.getError() != null) {
                loginViewModel.setLoginErrorResult(error.getError().getMessage());
            }
        });
    }

    /**
     * When the fragment is destroyed, removes the listener.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        binding = null;
    }

    /**
     * Listen for authentication changes, if the login is successful, this listener will be triggered,
     * sending the user to the app.
     * @param firebaseAuth - the authentication api from firebase that will tell if the user is authenticated or not.
     */
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            binding.loading.setVisibility(View.GONE);
            // Obtains the user's data in order to save it on a Shared preference.
            loginViewModel.retrieveCurrentUserData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    // Obtains the user's data and saves it on an app file with Shared preferences.
                    SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(SharedPrefRepository.SAVED_USERNAME_KEY, user.getUsername());
                    editor.putString(SharedPrefRepository.SAVED_PHOTO_URL_KEY, user.getPhotoUrl());

                    editor.apply();

                    NavHostFragment.findNavController(this).navigate(R.id.action_auth_navigation_to_navigation_home);
                } else {
                    Snackbar.make(binding.getRoot(), R.string.username_data_error, BaseTransientBottomBar.LENGTH_LONG).show();
                }
            });


        }
    }
}