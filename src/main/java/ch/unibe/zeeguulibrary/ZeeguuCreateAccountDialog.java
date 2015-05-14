package ch.unibe.zeeguulibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ch.unibe.R;

/**
 * Zeeguu Application
 * Created by Pascal on 12/05/15.
 */
public class ZeeguuCreateAccountDialog extends DialogFragment {
    private String email = "";
    private String username = "";

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextpassword;

    private ZeeguuConnectionManager connectionManager;
    private ZeeguuCreateAccountDialogCallbacks callback;


    /**
     *  Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuCreateAccountDialogCallbacks {
        ZeeguuConnectionManager getConnectionManager();

        void showZeeguuLoginDialog(String title, String tmpEmail);

        void toast(String text);

        void log(String text);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View mainView = inflater.inflate(R.layout.dialog_new_account, null);

        editTextUsername = (EditText) mainView.findViewById(R.id.dialog_username);
        editTextEmail = (EditText) mainView.findViewById(R.id.dialog_email);
        editTextpassword = (EditText) mainView.findViewById(R.id.dialog_password);

        editTextUsername.setText(username);
        editTextEmail.setText(email);

        builder.setView(mainView)
                .setPositiveButton(R.string.create_account, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String username = editTextUsername.getText().toString();
                        String email = editTextEmail.getText().toString();
                        String password = editTextpassword.getText().toString();

                        ZeeguuAccount account = connectionManager.getAccount();

                        if (email.equals("") || password.equals("") || username.equals("")) {
                            // TODO: Toast callbacks
//                            callback.toast(getActivity().getString(R.string.error_userinfo_invalid));
                            createNewAccount(email, username);
                        } else if (!account.isEmailValid(email)) {
//                            callback.toast(getActivity().getString(R.string.error_email_not_valid));
                            createNewAccount(email, username);
                        } else {
                            connectionManager.createAccountOnServer(username, email, password);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        final Dialog dialog = builder.create();

        TextView noAccountMessage = (TextView) mainView.findViewById(R.id.dialog_sign_in_no_account_textview);
        noAccountMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                //open login screen with last entered email address
                EditText editTextEmail = (EditText) dialog.findViewById(R.id.dialog_email);
                callback.showZeeguuLoginDialog("", editTextEmail.getText().toString());
            }
        });
        return dialog;
    }

    public void createNewAccount(String tmpEmail, String tmpUsername) {
        //if email or username already entered, reload them
        if (tmpEmail != null)
            email = tmpEmail;
        if (tmpUsername != null)
            username = tmpUsername;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuCreateAccountDialogCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuLoginDialogCallbacks");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("username", editTextUsername.getText().toString());
        savedInstanceState.putString("password", editTextEmail.getText().toString());
    }

    /**
     * Creates the dialog to confirm the logout task
     */
    public static class ZeeguuLogoutDialog extends DialogFragment {
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
}
