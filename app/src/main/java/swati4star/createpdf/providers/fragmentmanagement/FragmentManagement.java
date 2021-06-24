package swati4star.createpdf.providers.fragmentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import java.util.Objects;

import swati4star.createpdf.R;
import swati4star.createpdf.fragment.HomeFragment;
import swati4star.createpdf.fragment.ImageToPdfFragment;
import swati4star.createpdf.fragment.ViewFilesFragment;
//import swati4star.createpdf.util.FeedbackUtils;
//import swati4star.createpdf.util.FragmentUtils;

//import static swati4star.createpdf.util.Constants.ACTION_SELECT_IMAGES;
//import static swati4star.createpdf.util.Constants.ACTION_VIEW_FILES;
//import static swati4star.createpdf.util.Constants.ADD_WATERMARK;
//import static swati4star.createpdf.util.Constants.BUNDLE_DATA;
//import static swati4star.createpdf.util.Constants.OPEN_SELECT_IMAGES;
//import static swati4star.createpdf.util.Constants.ROTATE_PAGES;
//import static swati4star.createpdf.util.Constants.SHOW_WELCOME_ACT;

/**
 * This is a fragment service that manages the fragments
 * mainly for the MainActivity.
 */
public class FragmentManagement implements IFragmentManagement {
    private final FragmentActivity mContext;
    private final NavigationView mNavigationView;
    private boolean mDoubleBackToExitPressedOnce = false;
//    private final FeedbackUtils mFeedbackUtils;
//    private final FragmentUtils mFragmentUtils;

    public FragmentManagement(FragmentActivity context, NavigationView navigationView) {
        mContext = context;
        mNavigationView = navigationView;
//        mFeedbackUtils = new FeedbackUtils(mContext);
//        mFragmentUtils = new FragmentUtils(mContext);
    }

    @Override
    public void favouritesFragmentOption() {

    }

    public Fragment checkForAppShortcutClicked() {
        Fragment fragment = new HomeFragment();
        if (mContext.getIntent().getAction() != null) {
            switch (Objects.requireNonNull(mContext.getIntent().getAction())) {
                case "android.intent.action.SELECT_IMAGES":
                    fragment = new ImageToPdfFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("open_select_images", true);
                    fragment.setArguments(bundle);
                    break;
                case "android.intent.action.VIEW_FILES":
                    fragment = new ViewFilesFragment();
                    setNavigationViewSelection(R.id.nav_gallery);
                    break;
                default:
                    fragment = new HomeFragment(); // Set default fragment
                    break;
            }
        }
        if (areImagesReceived())
            fragment = new ImageToPdfFragment();

        mContext.getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();

        return fragment;
    }

    public boolean handleBackPressed() {
        Fragment currentFragment = mContext.getSupportFragmentManager()
                .findFragmentById(R.id.content);
        if (currentFragment instanceof HomeFragment) {
            return checkDoubleBackPress();
        }
        handleBackStackEntry();
        return false;
    }

    public boolean handleNavigationItemSelected(int itemId) {
        Fragment fragment = null;
        FragmentManager fragmentManager = mContext.getSupportFragmentManager();
        Bundle bundle = new Bundle();

        switch (itemId) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                break;
            case R.id.nav_camera:
                fragment = new ImageToPdfFragment();
                break;

            case R.id.nav_gallery:
                fragment = new ViewFilesFragment();
                break;

        }

        try {
            if (fragment != null)
                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // if help or share or what's new is clicked then return false, as we don't want
        // them to be selected
        return itemId != R.id.nav_share && itemId != R.id.nav_help
                && itemId != R.id.nav_whatsNew;
    }

    /**
     * Closes the app only when double clicked
     */
    private boolean checkDoubleBackPress() {
        if (mDoubleBackToExitPressedOnce) {
            return true;
        }
        mDoubleBackToExitPressedOnce = true;
        Toast.makeText(mContext, R.string.confirm_exit_message, Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Back stack count will be 1 when we open a item from favourite menu
     * on clicking back, return back to fav menu and change title
     */
    private void handleBackStackEntry() {
        int count = mContext.getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            String s = mContext.getSupportFragmentManager().getBackStackEntryAt(count - 1).getName();
            mContext.setTitle(s);
            mContext.getSupportFragmentManager().popBackStack();
        } else {
            Fragment fragment = new HomeFragment();
            mContext.getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
            mContext.setTitle(R.string.app_name);
            setNavigationViewSelection(R.id.nav_home);
        }
    }

    private boolean areImagesReceived() {
        Intent intent = mContext.getIntent();
        String type = intent.getType();
        return type != null && type.startsWith("image/");
    }

    private void setNavigationViewSelection(int id) {
        mNavigationView.setCheckedItem(id);
    }
}
