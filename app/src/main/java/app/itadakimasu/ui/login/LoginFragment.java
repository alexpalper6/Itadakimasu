package app.itadakimasu.ui.login;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import app.itadakimasu.data.Result;
import app.itadakimasu.databinding.FragmentLoginBinding;

import app.itadakimasu.R;

public class LoginFragment extends Fragment implements FirebaseAuth.AuthStateListener {

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

        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<LoginFormState>() {
            @Override
            public void onChanged(LoginFormState loginFormState) {
                if (loginFormState.getUserEmailError() != null) {
                    binding.etEmail.setError(getString(loginFormState.getUserEmailError()));
                }

                binding.btLogin.setEnabled(loginFormState.isDataValid());
            }
        });

        loginViewModel.getLoginErrorResult().observe(getViewLifecycleOwner(), new Observer<LoginErrorResult>() {
            @Override
            public void onChanged(LoginErrorResult loginResultState) {
                binding.loading.setVisibility(View.GONE);
                Snackbar.make(requireActivity().findViewById(android.R.id.content)
                        , loginResultState.getError()
                        , BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });

        binding.btRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_login_to_navigation_register)
        );

        binding.btLogin.setOnClickListener(v -> login(binding.etEmail.getText().toString(), binding.etPassword.getText().toString()));

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

        binding.etEmail.addTextChangedListener(afterTextChangedListener);
        binding.etPassword.addTextChangedListener(afterTextChangedListener);

    }

    private void login(String userEmail, String password) {
        binding.loading.setVisibility(View.VISIBLE);
        loginViewModel.login(userEmail, password).observe(getViewLifecycleOwner(), new Observer<Result.Error>() {
            @Override
            public void onChanged(Result.Error error) {
                if (error.getError() != null) {
                    loginViewModel.setLoginErrorResult(error.getError().getMessage());
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        binding = null;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            NavHostFragment.findNavController(this).navigate(R.id.action_auth_navigation_to_navigation_home);

        }
    }
}