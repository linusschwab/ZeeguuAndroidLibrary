package ch.unibe.zeeguulibrary.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import ch.unibe.R;

/**
 * Creates the dialog to confirm the logout task
 */
public class ZeeguuLogoutDialog extends DialogFragment {
    private ZeeguuDialogCallbacks callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        else
            builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.logout_zeeguu_confirmation);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.logout_zeeguu_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.getConnectionManager().getAccount().logout();
                    }
                }
        );

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dlg, int which) {
                        dlg.cancel();
                    }
                }
        );

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuDialogCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuDialogCallbacks");
        }
    }

}