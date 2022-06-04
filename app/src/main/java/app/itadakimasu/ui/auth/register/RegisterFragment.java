package app.itadakimasu.ui.auth.register;


import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.google.android.material.snackbar.Snackbar;


import app.itadakimasu.R;
import app.itadakimasu.data.Result;
import app.itadakimasu.data.model.User;
import app.itadakimasu.databinding.FragmentRegisterBinding;

/**
 * Fragment that lets the user interact with the system in order to register.
 */
@SuppressWarnings("unchecked")
public class RegisterFragment extends Fragment {
    // ViewModel that contains the UI States that this class will use to show the feedback to the user.
    private RegisterViewModel registerViewModel;
    // Class that contains the reference of every view of fragment_register layout.
    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Observes for data changes in the UI State's data input validation.
        // Then it will check the UI state for the error messages, which will be assigned to their
        // respective EditTexts or it will check if the data is valid.
        registerViewModel.getRegisterFormState().observe(getViewLifecycleOwner(), this::checkFormState);

        // Observes for the data error changes that is established by the registration.
        registerViewModel.getRegisterResult().observe(getViewLifecycleOwner(), this::checkErrorsRegisterResult);


        // Instantiate a TextWatcher that will update the state of  the RegisterFormState from the ViewModel.
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do here
            }

            /**
             * When a text changes on an EditTet, it will call the method registerDataChanged from the
             * ViewModel, updating the form state and checking if the every input is valid or not.
             * @param s - the editable text.
             */
            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(binding.etNewEmail.getText().toString(), binding.etNewUsername.getText().toString(),
                        binding.etNewPassword.getText().toString(), binding.etRepeatPassword.getText().toString());
            }
        };

        // Set the TextWatcher to each EditText.
        setTextListeners(afterTextChangedListener);

        // The last EditText has an imeOption called actionDone, that option is called when pressed enter.
        // This method will listen for this option, when it's activated, it'll use the register method.
        binding.etRepeatPassword.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createAccount();
            }
            return false;
        });

        // Establish a click listener on create button account, when clicked, register method will be called.
        binding.btCreateAccount.setOnClickListener(v -> createAccount());

    }

    /**
     * Checks for the errors that happens when the register result's data is changed.
     * @param registerResult - the register result data obtained when the live data changes on the view model.
     */
    private void checkErrorsRegisterResult(RegisterErrorResult registerResult) {
        if (registerResult == null) {
            return;
        }
        binding.pbRegisterProgress.setVisibility(View.GONE);
        // If the error is a username one, shows the error to the user and clears the username's field.
        if (registerResult.getUsernameError() != null) {
            binding.etNewUsername.setText("");
            Snackbar.make(binding.getRoot(), registerResult.getUsernameError(), Snackbar.LENGTH_LONG).show();

        }
        // If the transaction fails, the registry error result will have the user's data
        // so a SnackBar will appear asking the user for retry.
        if (registerResult.getUser() != null && registerResult.getError() != null) {
            Snackbar.make(binding.getRoot(), registerResult.getError(), Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, v -> completeRegisterTransaction(registerResult.getUser()))
                    .show();
        }

        // If it fails but the user is not added, then the user couldn't be registered..
        if (registerResult.getError() != null && registerResult.getUser() == null) {
            Snackbar.make(binding.getRoot(), registerResult.getError(), Snackbar.LENGTH_LONG).show();
            binding.btCreateAccount.setEnabled(true);
        }
    }

    /**
     * The user will try to create the account, the username will be search on the database, if it doesn't exist,
     * then the registry will be performed.
     * If the user exists, it update the resultFormState with the username error, telling the user that the username
     * is already chosen, or another error that could have happened.
     */
    private void createAccount() {
        registerViewModel.isUsernameChosen(binding.etNewUsername.getText().toString()).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {

                boolean usernameIsFound = ((Result.Success<Boolean>) result).getData();
                if (!usernameIsFound) {
                    register();
                    return;
                }
            }
            registerViewModel.setUsernameResultError(result);
        });
        // The account creation button will be disabled, we don't want the user to perform the same
        // registry multiple times
        binding.btCreateAccount.setEnabled(false);
        binding.pbRegisterProgress.setVisibility(View.VISIBLE);
    }

    /**
     * Perform the registry with ViewModel's method, using the input fields as the data used.
     * It will observe for the returned result. When it's returned, if the result is successful,
     * it will continue the registry transaction (the user will be uploaded to the database).
     * If not, it will show an error by updating the registry error result.
     */
    private void register() {
        registerViewModel.register(binding.etNewEmail.getText().toString(), binding.etNewUsername.getText().toString(),
                binding.etNewPassword.getText().toString()).observe(getViewLifecycleOwner(), result -> {
                    if (result instanceof Result.Success) {
                        User user = ((Result.Success<User>) result).getData();
                        completeRegisterTransaction(user);
                    } else if (result instanceof Result.Error) {
                        registerViewModel.setRegisterError(((Result.Error) result).getError().getMessage());
                    }
                });
    }

    /**
     * Adds the user to the database, completing the registry transaction.
     * If the result is successful, the user will be redirected to the next part of the registry, where
     * they can upload a profile picture or not.
     * If it is not successful, the register error result will be updated.
     * The app will save the username and photo with SharedPreferences so in the future the app will be
     * able to retrieve the username data from there instead reading every time from the database.
     * @param user - the user's data.
     */
    private void completeRegisterTransaction(User user) {
        registerViewModel.addUserToDatabase(user).observe(getViewLifecycleOwner(), result -> {
            if (result instanceof Result.Success) {
                binding.pbRegisterProgress.setVisibility(View.GONE);

                registerViewModel.setAuthUsername(user.getUsername());
                registerViewModel.setAuthUserPhotoUrl(user.getPhotoUrl());


                NavHostFragment.findNavController(requireParentFragment()).navigate(R.id.action_navigation_register_to_addPhotoFragment);
            } else if (result instanceof  Result.Error){
                registerViewModel.setUserUploadError(((Result.Error) result).getError().getMessage(), user);
            }
        });
    }

    /**
     * Establish each EditText with the TextWatcher.
     *
     * @param afterTextChangedListener - the TextWatcher that will notify and update the RegisterFormState
     *                                 from the ViewModel.
     */
    private void setTextListeners(TextWatcher afterTextChangedListener) {
        binding.etNewEmail.addTextChangedListener(afterTextChangedListener);
        binding.etNewUsername.addTextChangedListener(afterTextChangedListener);
        binding.etNewPassword.addTextChangedListener(afterTextChangedListener);
        binding.etRepeatPassword.addTextChangedListener(afterTextChangedListener);
    }

    /**
     * @param registerFormState - the UI State that contains the error message or the data validation
     *                          boolean.
     */
    private void checkFormState(RegisterFormState registerFormState) {
        if (registerFormState == null) {
            return;
        }

        binding.btCreateAccount.setEnabled(registerFormState.isDataValid());

        if (registerFormState.getEmailError() != null) {
            binding.tilNewEmail.setError(getString(registerFormState.getEmailError()));
        } else {
            binding.tilNewEmail.setError(null);
        }

        if (registerFormState.getUsernameError() != null) {
            binding.tilNewUsername.setError(getString(registerFormState.getUsernameError()));
        } else {
            binding.tilNewUsername.setError(null);
        }

        if (registerFormState.getPasswordError() != null) {
            binding.tilNewPassword.setError(getString(registerFormState.getPasswordError()));
        } else {
            binding.tilNewPassword.setError(null);
        }

        if (registerFormState.getRepeatedPasswordError() != null) {
            binding.tilRepeatPassword.setError(getString(registerFormState.getRepeatedPasswordError()));
        } else {
            binding.tilRepeatPassword.setError(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}