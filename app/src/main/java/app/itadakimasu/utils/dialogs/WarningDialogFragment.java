package app.itadakimasu.utils.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import app.itadakimasu.R;

/**
 * Dialog fragment created to alert the user with a dialog with a custom warning dialog and message,
 * passed as an argument.
 */
public class WarningDialogFragment extends DialogFragment {
    public static final String TAG = "app.itadakimasu.utils.dialogs.WarningDialogFragment.TAG";
    public static final String DIALOG_TITLE = "app.itadakimasu.utils.dialogs.WarningDialogFragment.Title";
    public static final String DIALOG_MESSAGE = "app.itadakimasu.utils.dialogs.WarningDialogFragment.Message";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());

        Bundle bundle = getArguments();
        int title = bundle.getInt(DIALOG_TITLE);
        int message = bundle.getInt(DIALOG_MESSAGE);

        builder.setTitle(title).setMessage(message).setNeutralButton(R.string.ok, (dialog, which) -> {
            WarningDialogFragment.this.getDialog().dismiss();
        });

        return builder.create();
    }
}
