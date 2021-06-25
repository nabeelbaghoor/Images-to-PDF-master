package nabeelbaghoor.I2PConverterApp.util;

public interface OnPDFCreatedInterface {
    void onPDFCreationStarted();

    void onPDFCreated(boolean success, String path);
}
