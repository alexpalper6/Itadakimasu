package app.itadakimasu.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import app.itadakimasu.R;

/**
 * Dialog fragment that will display when the user wants to adds an image
 */
public class SelectMediaDialogFragment extends DialogFragment {
    // Identificator of the DialogFragment that is used on DialogFragment.show();
    public static String TAG = "SelectMediaDialog";
    // Dialog keys for request and bundle
    public static String DIALOG_REQUEST = "app.itadakimasu.ui.SelectMediaDialogFragment.DialogRequest";
    public static String DIALOG_RESULT = "app.itadakimasu.ui.SelectMediaDialogFragment.DialogResult";


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Creates a dialog with the option of taking a photo and select from gallery
        builder.setTitle(R.string.select_media_method).setItems(R.array.media_selectors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Bundle result = new Bundle();
                result.putInt(DIALOG_RESULT, which);
                getParentFragmentManager().setFragmentResult(DIALOG_REQUEST, result);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                SelectMediaDialogFragment.this.getDialog().cancel();
            }
        });
        return builder.create();
    }


}
