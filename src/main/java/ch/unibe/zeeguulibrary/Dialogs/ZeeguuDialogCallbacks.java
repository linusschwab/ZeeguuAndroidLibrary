package ch.unibe.zeeguulibrary.Dialogs;

import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 * Callback interface that must be implemented by the container activity
 */
public interface ZeeguuDialogCallbacks {
    ZeeguuConnectionManager getConnectionManager();
    void showZeeguuLoginDialog(String title, String email);
    void displayMessage(String message);
}
