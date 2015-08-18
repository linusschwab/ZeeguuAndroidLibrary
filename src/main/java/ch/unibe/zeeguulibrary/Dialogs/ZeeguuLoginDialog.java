package ch.unibe.zeeguulibrary.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import ch.unibe.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

public class ZeeguuLoginDialog extends DialogFragment {
    private String message = "";
    private String email = "";

    private ImageButton newAccountButton;
    private TextView messageTextView;
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
        View mainView = inflater.inflate(R.layout.dialog_zeeguu_login, null);

        // Create Account Button onClickListener
        newAccountButton = (ImageButton) mainView.findViewById(R.id.login_header_new_account);
        newAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                callback.showZeeguuCreateAccountDialog("", email, "");
            }
        });

        // Display Message
        messageTextView = (TextView) mainView.findViewById(R.id.login_zeeguu_message);
        if (!message.equals("")) {
            messageTextView.setText(message);
            messageTextView.setVisibility(View.VISIBLE);
        }

        emailEditText = (EditText) mainView.findViewById(R.id.login_zeeguu_email);
        passwordEditText = (EditText) mainView.findViewById(R.id.login_zeeguu_password);

        if (savedInstanceState != null) {
            emailEditText.setText(savedInstanceState.getString("email"));
            passwordEditText.setText(savedInstanceState.getString("password"));
        }

        // Keep email if password was wrong
        emailEditText.setText(email);

        // Highlight missing/wrong information
        if (message.equals(getActivity().getString(R.string.error_email)))
            highlightEditText(emailEditText);
        else if (message.equals(getActivity().getString(R.string.login_zeeguu_error_password))) {
            highlightEditText(passwordEditText);
            passwordEditText.requestFocus();
            // TODO: keep keyboard open
        }

        connectionManager = callback.getZeeguuConnectionManager();

        builder.setView(mainView)
                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String email = emailEditText.getText().toString();
                        String password = passwordEditText.getText().toString();

                        if (!connectionManager.isNetworkAvailable()) {
                            dismiss();
                            callback.displayMessage(getActivity().getString(R.string.error_no_internet_connection));
                        } else if (!connectionManager.getAccount().isEmailValid(email)) {
                            dismiss();
                            callback.showZeeguuLoginDialog(getActivity().getString(R.string.error_email), null);
                        } else if (password.equals("")) {
                            dismiss();
                            callback.showZeeguuLoginDialog(getActivity().getString(R.string.login_zeeguu_error_password), email);
                        }
                        // Try to get a session ID to check if the password is correct
                        else
                            connectionManager.getSessionId(email, password);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
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
        savedInstanceState.putString("email", emailEditText.getText().toString());
        savedInstanceState.putString("password", passwordEditText.getText().toString());
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setTextColor(Color.BLACK);
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
     * Allows to set an email address, for example if the user entered a wrong password, then he
     * does not have to enter a new email address
     * Must be called before the DialogFragment is shown!
     */
    public void setEmail(String email) {
        if (email != null)
            this.email = email;
    }
}