package app.itadakimasu.utils.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import app.itadakimasu.R;

/**
 * Dialog fragment that will display when the user wants to adds an image.
 */
public class SelectMediaDialogFragment extends DialogFragment {
    // Identificator of the DialogFragment that is used on DialogFragment.show();
    public static String TAG = "SelectMediaDialog";
    // Dialog keys for request and bundle
    public static String DIALOG_REQUEST = "app.itadakimasu.utils.dialogs.SelectMediaDialogFragment.DialogRequest";
    public static String DIALOG_RESULT = "app.itadakimasu.utils.dialogs.SelectMediaDialogFragment.DialogResult";

    public static final int TAKING_PHOTO_CAMERA = 0;
    public static final int IMAGE_GALLERY = 1;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        // Creates a dialog with the option of taking a photo and select from gallery
        builder.setTitle(R.string.select_media_method).setItems(R.array.media_selectors, (dialog, which) -> {
            Bundle result = new Bundle();
            result.putInt(DIALOG_RESULT, which);
            getParentFragmentManager().setFragmentResult(DIALOG_REQUEST, result);
        }).setNegativeButton(R.string.cancel, (dialog, id) -> SelectMediaDialogFragment.this.getDialog().cancel());
        return builder.create();
    }


}
