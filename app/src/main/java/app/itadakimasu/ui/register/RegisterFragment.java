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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.firestore.FirebaseFirestoreException;

import app.itadakimasu.data.Result;
import app.itadakimasu.databinding.FragmentRegisterBinding;

/**
 * Fragment that let the user interact with the system in order to register.
 */
public class RegisterFragment extends Fragment {
    // ViewModel that contains the UI States that this class will use to show the feedback to the user.
    private RegisterViewModel registerViewModel;
    // Class that contains the reference of every view of fragment_register layout.
    private FragmentRegisterBinding binding;

    // References of the views, TODO Maybe this will be deleted
    private EditText etNewEmail;
    private EditText etNewUsername;
    private EditText etNewPassword;
    private EditText etRepeatPassword;
    private Button btCreateAccount;
    private ProgressBar pbRegProgress;

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

        setBindingReferences();

        // Observes for data changes in the UI State's data input validation.
        // Then it will check the UI state for the error messages, which will be assigned to their
        // respective EditTexts or it will check if the data is valid.
        registerViewModel.getRegisterFormState().observe(getViewLifecycleOwner(), registerFormState -> checkFormState(registerFormState));

        // Observes for the data changes that is established by the registration.
        // If the result is successful, then the user will be sent to the next fragment.
        // If it's not, then the error message will prompt.
        registerViewModel.getRegisterResult().observe(getViewLifecycleOwner(), registerResult -> {
            if (registerResult == null) {
                return;
            }
            pbRegProgress.setVisibility(View.GONE);

            if (registerResult.getUsernameError() != null) {
                etNewUsername.setText("");
                if (getContext() != null && getContext().getApplicationContext() != null) {
                    Toast.makeText(
                            getContext().getApplicationContext(),
                            registerResult.getUsernameError(),
                            Toast.LENGTH_LONG).show();
                }
            }

            if (registerResult.getUser() != null) {
                //TODO: Send user to add photo
            }

            if (registerResult.getError() != null) {
                if (getContext() != null && getContext().getApplicationContext() != null) {
                    Toast.makeText(
                            getContext().getApplicationContext(),
                            registerResult.getError(),
                            Toast.LENGTH_LONG).show();
                }
                btCreateAccount.setEnabled(true);
            }
        });

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
                registerViewModel.registerDataChanged(etNewEmail.getText().toString(), etNewUsername.getText().toString(),
                        etNewPassword.getText().toString(), etRepeatPassword.getText().toString());
            }
        };

        // Set the TextWatcher to each EditText.
        setTextListeners(afterTextChangedListener);

        // The last EditText has an imeOption called actionDone, that option is called when pressed enter.
        // This method will listen for this option, when it's activated, it'll use the register method.
        etRepeatPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    createAccount();
                }
                return false;
            }
        });

        // Establish a click listener on create button account, when clicked, register method will be called.
        btCreateAccount.setOnClickListener(v -> createAccount());

    }

    /**
     * Set the reference variables with their respective views.
     */
    private void setBindingReferences() {
        etNewEmail = binding.etNewEmail;
        etNewUsername = binding.etNewUsername;
        etNewPassword = binding.etNewPassword;
        etRepeatPassword = binding.etRepeatPassword;
        btCreateAccount = binding.btCreateAccount;
        pbRegProgress = binding.pbRegisterProgress;
    }

    /**
     * The user will try to create the account, the username will be search on the database, if it doesn't exist,
     * then the registry will be performed.
     * If the user exists, it update the resultFormState with the username error, telling the user that the username
     * is already chosen, or another error that could have happened.
     */
    private void createAccount() {
        registerViewModel.isUsernameChosen(etNewUsername.getText().toString()).observe(getViewLifecycleOwner(), new Observer<Result<?>>() {
            @Override
            public void onChanged(Result<?> result) {
                if (result instanceof Result.Success) {
                    boolean usernameIsFound = ((Result.Success<Boolean>) result).getData();
                    if (!usernameIsFound) {
                        register();
                        return;
                    }
                }
                registerViewModel.setUsernameResultError(result);
            }
        });
        // The account creation button will be disabled, we don't want the user to perform the same
        // registry multiple times
        btCreateAccount.setEnabled(false);
        pbRegProgress.setVisibility(View.VISIBLE);
    }

    /**
     * Perform the registry with ViewModel's method, using the input fields as the data used.
     * It will observe for the returned result. When it's returned, it will notify that the
     * registerResult has changed, using registerResultChanged from the ViewModel.
     */
    private void register() {
        registerViewModel.register(etNewEmail.getText().toString(), etNewUsername.getText().toString(),
                etNewPassword.getText().toString()).observe(getViewLifecycleOwner(), new Observer<Result>() {
            @Override
            public void onChanged(Result result) {
                registerViewModel.registerResultChanged(result);
            }
        });

    }

    /**
     * Establish each EditText with the TextWatcher.
     * @param afterTextChangedListener - the TextWatcher that will notify and update the RegisterFormState
     *                                 from the ViewModel.
     */
    private void setTextListeners(TextWatcher afterTextChangedListener) {
        etNewEmail.addTextChangedListener(afterTextChangedListener);
        etNewUsername.addTextChangedListener(afterTextChangedListener);
        etNewPassword.addTextChangedListener(afterTextChangedListener);
        etRepeatPassword.addTextChangedListener(afterTextChangedListener);
    }

    /**
     *
     * @param registerFormState - the UI State that contains the error message or the data validation
     *                          boolean.
     */
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