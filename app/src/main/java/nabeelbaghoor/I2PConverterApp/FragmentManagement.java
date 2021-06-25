package nabeelbaghoor.I2PConverterApp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Objects;

import nabeelbaghoor.I2PConverterApp.R;

//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;

/**
 * This is a fragment service that manages the fragments
 * mainly for the MainActivity.
 */
public class FragmentManagement {
    private final FragmentActivity mContext;

    public FragmentManagement(MainActivity context) {
        mContext = context;
    }

    public Fragment checkForAppShortcutClicked() {
        HomeFragment fragment = new HomeFragment();
        if (mContext.getIntent().getAction() != null) {
            switch (Objects.requireNonNull(mContext.getIntent().getAction())) {
                case "android.intent.action.SELECT_IMAGES":
                    fragment = new HomeFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("open_select_images", true);
                    fragment.setArguments(bundle);
                    break;
                default:
                    fragment = new HomeFragment(); // Set default fragment
                    break;
            }
        }
        if (areImagesReceived())
            fragment = new HomeFragment();

        mContext.getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();

        return fragment;
    }

    private boolean areImagesReceived() {
        Intent intent = mContext.getIntent();
        String type = intent.getType();
        return type != null && type.startsWith("image/");
    }
}
