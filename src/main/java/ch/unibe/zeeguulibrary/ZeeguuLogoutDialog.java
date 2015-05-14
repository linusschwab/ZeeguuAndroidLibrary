package ch.unibe.zeeguulibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import ch.unibe.R;

/**
 * Creates the dialog to confirm the logout task
 */
public class ZeeguuLogoutDialog extends DialogFragment {
    private ZeeguuLogoutDialogCallbacks callback;

    /**
     *  Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuLogoutDialogCallbacks {
        ZeeguuConnectionManager getConnectionManager();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.logout_zeeguu_confirmation);
        dialog.setCancelable(true);
        dialog.setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.getConnectionManager().getAccount().logout();
                    }
                }

        );

        dialog.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener()

                {
                    @Override
                    public void onClick(DialogInterface dlg, int which) {
                        dlg.cancel();
                    }
                }

        );

        return dialog.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuLogoutDialogCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuLogoutDialogCallbacks");
        }
    }

}