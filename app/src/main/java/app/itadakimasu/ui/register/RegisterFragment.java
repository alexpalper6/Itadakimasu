package app.itadakimasu.ui.register;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import app.itadakimasu.databinding.RegisterFragmentBinding;

public class RegisterFragment extends Fragment {

    private RegisterViewModel registerViewModel;
    private RegisterFragmentBinding binding;
    private EditText etNewEmail;
    private EditText etNewUsername;
    private EditText etNewPassword;
    private EditText etRepeatPassword;
    private Button btCreateAccount;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = RegisterFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        etNewEmail = binding.etNewEmail;
        etNewUsername = binding.etNewUsername;
        etNewPassword = binding.etNewPassword;
        etRepeatPassword = binding.etRepeatPassword;
        btCreateAccount = binding.btCreateAccount;

        registerViewModel.getRegisterFormState().observe(getViewLifecycleOwner(), registerFormState -> checkFormState(registerFormState));

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do here
            }

            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(etNewEmail.getText().toString(), etNewUsername.getText().toString(),
                        etNewPassword.getText().toString(), etRepeatPassword.getText().toString());
            }
        };
        setTextListeners(etNewEmail, etNewUsername, etNewPassword, etRepeatPassword, afterTextChangedListener);
        etRepeatPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //TODO: Register from viewModel
                }
                return false;
            }
        });

    }

    private void setTextListeners(EditText etNewEmail, EditText etNewUsername, EditText etNewPassword, EditText etRepeatPassword, TextWatcher afterTextChangedListener) {
        etNewEmail.addTextChangedListener(afterTextChangedListener);
        etNewUsername.addTextChangedListener(afterTextChangedListener);
        etNewPassword.addTextChangedListener(afterTextChangedListener);
        etRepeatPassword.addTextChangedListener(afterTextChangedListener);
    }

    private void checkFormState(RegisterFormState registerFormState) {
        if (registerFormState == null) {
            return;
        }

        btCreateAccount.setEnabled(registerFormState.isDataValid());

        if (registerFormState.getEmailError() != null) {
            etNewEmail.setError(getString(registerFormState.getEmailError()));
        }
        if (registerFormState.getUsernameError() != null) {
            etNewUsername.setError(getString(registerFormState.getUsernameError()));
        }
        if (registerFormState.getPasswordError() != null) {
            etNewPassword.setError(getString(registerFormState.getPasswordError()));
        }
        if (registerFormState.getRepeatedPasswordError() != null) {
            etRepeatPassword.setError(getString(registerFormState.getRepeatedPasswordError()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}