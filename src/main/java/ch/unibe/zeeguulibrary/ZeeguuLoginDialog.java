package ch.unibe.zeeguulibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ch.unibe.R;

public class ZeeguuLoginDialog extends DialogFragment {
    private String title = "Zeeguu Login";
    private String email = "";

    private EditText emailEditText;
    private EditText passwordEditText;

    private ZeeguuConnectionManager connectionManager;
    private ZeeguuLoginDialogCallbacks callback;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuLoginDialogCallbacks {
        ZeeguuConnectionManager getConnectionManager();
        void showZeeguuLoginDialog(String title, String email);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_zeeguu_login, null);

        emailEditText = (EditText) mainView.findViewById(R.id.email);
        passwordEditText = (EditText) mainView.findViewById(R.id.password);

        if (savedInstanceState != null) {
            emailEditText.setText(savedInstanceState.getString("email"));
            passwordEditText.setText(savedInstanceState.getString("password"));
        }

        // Keep email if password was wrong
        emailEditText.setText(email);

        // Highlight missing/wrong information
        if (title.equals(getActivity().getString(R.string.login_zeeguu_error_email)))
            highlightEditText(emailEditText);
        else if (title.equals(getActivity().getString(R.string.login_zeeguu_error_password))) {
            highlightEditText(passwordEditText);
            passwordEditText.requestFocus();
            // TODO: keep keyboard open
        }

        connectionManager = callback.getConnectionManager();

        builder.setMessage(title);
        builder.setView(mainView)
                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String email = emailEditText.getText().toString();
                        String password = passwordEditText.getText().toString();

                        if (!connectionManager.getAccount().isEmailValid(email)) {
                            dismiss();
                            callback.showZeeguuLoginDialog(getActivity().getString(R.string.login_zeeguu_error_email), null);
                        }
                        else if (password.equals("")) {
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
            callback = (ZeeguuLoginDialogCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuLoginDialogCallbacks");
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
        editText.setHintTextColor(Color.RED);
    }

    /**
     * Allows to set a different title, for example if the user entered a wrong password.
     * Must be called before the DialogFragment is shown!
     */
    public void setTitle(String title) {
        if (title != null || !title.equals(""))
            this.title = title;
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