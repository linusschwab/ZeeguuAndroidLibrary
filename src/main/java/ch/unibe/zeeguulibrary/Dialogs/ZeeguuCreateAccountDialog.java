package ch.unibe.zeeguulibrary.Dialogs;

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
import ch.unibe.zeeguulibrary.Core.ZeeguuAccount;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

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
    private ZeeguuDialogCallbacks callback;


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
            callback = (ZeeguuDialogCallbacks) activity;
            connectionManager = callback.getConnectionManager();
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuDialogCallbacks");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("username", editTextUsername.getText().toString());
        savedInstanceState.putString("password", editTextEmail.getText().toString());
    }
}