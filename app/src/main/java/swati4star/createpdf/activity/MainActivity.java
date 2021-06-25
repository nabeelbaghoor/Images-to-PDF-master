package swati4star.createpdf.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.util.ArrayList;

import swati4star.createpdf.R;
import swati4star.createpdf.providers.fragmentmanagement.FragmentManagement;

public class MainActivity extends AppCompatActivity {

    public static final String[] READ_WRITE_CAMERA_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    public static final String[] READ_WRITE_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_REQUEST_CODE = 0;
    private FragmentManagement mFragmentManagement;

    public static void makeAndClearTemp() {
        String dest = Environment.getExternalStorageDirectory().toString() +
                "/PDFfiles/" + "temp";
        File folder = new File(dest);
        boolean result = folder.mkdir();

        // clear all the files in it, if any
        if (result && folder.isDirectory()) {
            String[] children = folder.list();
            for (String child : children) {
                new File(folder, child).delete();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize values
        initializeValues();

        // Check for app shortcuts & select default fragment
        Fragment fragment = mFragmentManagement.checkForAppShortcutClicked();

        // Check if  images are received
        handleReceivedImagesIntent(fragment);
        getRuntimePermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isStoragePermissionGranted()) {
            makeAndClearTemp();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.show();
    }

    private void initializeValues() {
        mFragmentManagement = new FragmentManagement(this);
    }

    private void handleReceivedImagesIntent(Fragment fragment) {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (type == null || !type.startsWith("image/"))
            return;

        if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            handleSendMultipleImages(intent, fragment); // Handle multiple images
        } else if (Intent.ACTION_SEND.equals(action)) {
            handleSendImage(intent, fragment); // Handle single image
        }
    }

    private void handleSendImage(Intent intent, Fragment fragment) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        ArrayList<Uri> imageUris = new ArrayList<>();
        imageUris.add(uri);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
        fragment.setArguments(bundle);
    }

    private void handleSendMultipleImages(Intent intent, Fragment fragment) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
            fragment.setArguments(bundle);
        }
    }

    public void requestRuntimePermissions(Object context, String[] permissions,
                                          int requestCode) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions((AppCompatActivity) context,
                    permissions, requestCode);
        } else if (context instanceof Fragment) {
            ((Fragment) context).requestPermissions(permissions, requestCode);
        }
    }

    private void getRuntimePermissions() {
        requestRuntimePermissions(this,
                READ_WRITE_CAMERA_PERMISSIONS,
                PERMISSION_REQUEST_CODE);
    }

    private Context retrieveContext(@NonNull Object context) {
        if (context instanceof AppCompatActivity) {
            return ((AppCompatActivity) context).getApplicationContext();
        } else {
            return ((Fragment) context).requireActivity();
        }
    }

    public boolean checkRuntimePermissions(Object context, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if ((ContextCompat.checkSelfPermission(retrieveContext(context),
                        permission)
                        != PackageManager.PERMISSION_GRANTED)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isStoragePermissionGranted() {
        return checkRuntimePermissions(this, READ_WRITE_PERMISSIONS);
    }
}
