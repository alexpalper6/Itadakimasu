package app.itadakimasu.utils.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import app.itadakimasu.R;
import app.itadakimasu.databinding.DialogAddDescriptionBinding;

public class AddDescriptionDialogFragment extends DialogFragment {
    public static final String TAG = "AddDescriptionDialogFragment";

    public static final String DIALOG_TITLE = "app.itadakimasu.utils.dialogs.AddDescriptionDialogFragment.DialogTitle";
    public static final String DIALOG_REQUEST = "app.itadakimasu.utils.dialogs.AddDescriptionDialogFragment.DialogTitle";
    public static final String DIALOG_RESULT = "app.itadakimasu.utils.dialogs.AddDescriptionDialogFragment.DialogResult";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        // Getting the layout inflater to set the layout on the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        DialogAddDescriptionBinding binding = DialogAddDescriptionBinding.inflate(inflater);

        int title = R.string.add_description;
        if (bundle != null) {
            title = bundle.getInt(DIALOG_TITLE);
        }

        builder.setTitle(title)
                // We set the layout, the root is null because this is only going to the dialog's layout
                .setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String description = binding.etIngredientInfo.getText().toString().trim();

                    if ("".equals(description)) {
                        AddDescriptionDialogFragment.this.getDialog().dismiss();
                    } else {
                        Bundle result = new Bundle();
                        result.putString(DIALOG_RESULT, binding.etIngredientInfo.getText().toString());
                        getParentFragmentManager().setFragmentResult(DIALOG_REQUEST, result);
                    }
                })
                .setNegativeButton(R.string.cancel, ((dialog, which) -> AddDescriptionDialogFragment.this.getDialog().cancel()));

        return builder.create();
    }
}
