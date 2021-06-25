package nabeelbaghoor.I2PConverterApp.util;

public interface EmptyStateChangeListener {
    void setEmptyStateVisible();

    void setEmptyStateInvisible();

    void showNoPermissionsView();

    void hideNoPermissionsView();

    void filesPopulated();
}
