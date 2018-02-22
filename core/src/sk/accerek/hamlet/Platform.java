package sk.accerek.hamlet;

public interface Platform {
    int DIALOG_ERROR_MESSAGE = 0;
    int DIALOG_INFO_MESSAGE = 1;
    int DIALOG_WARNING_MESSAGE = 2;
    int DIALOG_QUESTION_MESSAGE = 3;

    void nativeUpdate();
    void showNativeDialog(String title, String message, int type);

    boolean isFpsCapped();
    boolean isDebugEnabled();
}
