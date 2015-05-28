package ch.unibe.zeeguulibrary.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ch.unibe.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Dialog to create a new Zeeguu account on the server
 */
public class ZeeguuCreateAccountDialog extends DialogFragment {
    private String message = "";
    private String username = "";
    private String email = "";

    private TextView messageTextView;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;

    private ZeeguuConnectionManager connectionManager;
    private ZeeguuDialogCallbacks callback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        else
            builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_zeeguu_create_account, null);

        usernameEditText = (EditText) mainView.findViewById(R.id.create_account_username);
        emailEditText = (EditText) mainView.findViewById(R.id.create_account_email);
        passwordEditText = (EditText) mainView.findViewById(R.id.create_account_password);

        usernameEditText.setText(username);
        emailEditText.setText(email);

        // Display Message
        messageTextView = (TextView) mainView.findViewById(R.id.create_account_message);
        if (!message.equals("")) {
            messageTextView.setText(message);
            messageTextView.setVisibility(View.VISIBLE);
        }

        // Highlight missing/wrong information
        if (message.equals(getActivity().getString(R.string.create_account_error_username))) {
            highlightEditText(usernameEditText);
        } else if (message.equals(getActivity().getString(R.string.error_email))) {
            highlightEditText(emailEditText);
            emailEditText.requestFocus();
        } else if (message.equals(getActivity().getString(R.string.create_account_error_password))) {
            highlightEditText(passwordEditText);
            passwordEditText.requestFocus();
            // TODO: keep keyboard open
        }

        connectionManager = callback.getConnectionManager();

        builder.setView(mainView)
                .setPositiveButton(R.string.create_account, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String username = usernameEditText.getText().toString();
                        String email = emailEditText.getText().toString();
                        String password = passwordEditText.getText().toString();

                        if (username.equals("")) {
                            dismiss();
                            callback.showZeeguuCreateAccountDialog(getActivity().getString(R.string.create_account_error_username), "", email);
                        } else if (!connectionManager.getAccount().isEmailValid(email)) {
                            dismiss();
                            callback.showZeeguuCreateAccountDialog(getActivity().getString(R.string.error_email), username, "");
                        } else if (password.equals("")) {
                            dismiss();
                            callback.showZeeguuCreateAccountDialog(getActivity().getString(R.string.create_account_error_password), username, email);
                        } else {
                            connectionManager.createAccountOnServer(username, email, password);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        callback.showZeeguuLoginDialog("", "");
                    }
                });

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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("username", usernameEditText.getText().toString());
        savedInstanceState.putString("password", emailEditText.getText().toString());
    }

    private void highlightEditText(EditText editText) {
        editText.setHintTextColor(getResources().getColor(R.color.zeeguu_red));
    }

    /**
     * Allows to set a different message, for example if the user entered a wrong password.
     * Must be called before the DialogFragment is shown!
     */
    public void setMessage(String message) {
        if (message != null || !message.equals(""))
            this.message = message;
    }

    /**
     * Allows to set an username.
     * Must be called before the DialogFragment is shown!
     */
    public void setUsername(String username) {
        if (username != null)
            this.username = username;
    }

    /**
     * Allows to set an email address.
     * Must be called before the DialogFragment is shown!
     */
    public void setEmail(String email) {
        if (email != null)
            this.email = email;
    }
}