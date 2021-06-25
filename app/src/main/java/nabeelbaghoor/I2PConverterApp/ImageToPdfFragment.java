package nabeelbaghoor.I2PConverterApp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.morphingbutton.MorphingButton;
import com.google.android.material.snackbar.Snackbar;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nabeelbaghoor.I2PConverterApp.R;
import nabeelbaghoor.I2PConverterApp.util.OnPDFCreatedInterface;
import nabeelbaghoor.I2PConverterApp.util.CreatePdf;
import nabeelbaghoor.I2PConverterApp.util.FileUtils;
import nabeelbaghoor.I2PConverterApp.util.MorphButtonUtility;

public class ImageToPdfFragment extends Fragment implements
        OnPDFCreatedInterface {

    private static final ArrayList<String> mUnarrangedImagesUri = new ArrayList<>();
    public static ArrayList<String> mImagesUri = new ArrayList<>();
    @BindView(R.id.pdfCreate)
    MorphingButton mCreatePdf;
    @BindView(R.id.pdfOpen)
    MorphingButton mOpenPdf;
    //    @BindView(R.id.tvNoOfImages)
//    TextView mNoOfImages;
    private MorphButtonUtility mMorphButtonUtility;
    private Activity mActivity;
    private String mPath;
    private FileUtils mFileUtils;
    private int mPageColor;
    private boolean mIsButtonAlreadyClicked = false;
    private MaterialDialog mMaterialDialog;
    private String mHomePath;
    private boolean mPermissionGranted = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_images_to_pdf, container, false);
        ButterKnife.bind(this, root);

        // Initialize variables
        mPermissionGranted = checkRuntimePermissions(this,
                READ_WRITE_CAMERA_PERMISSIONS);
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        mFileUtils = new FileUtils(mActivity);
        mPageColor = Color.WHITE;
        mHomePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PDFfiles/";

        // Get default values & show enhancement options
        resetValues();

        // Check for the images received
        checkForImagesInBundle();

        if (mImagesUri.size() > 0) {
//            mNoOfImages.setText(String.format(mActivity.getResources()
//                    .getString(R.string.images_selected), mImagesUri.size()));
//            mNoOfImages.setVisibility(View.VISIBLE);
            mMorphButtonUtility.morphToSquare(mCreatePdf, mMorphButtonUtility.integer());
            mCreatePdf.setEnabled(true);
        } else {
//            mNoOfImages.setVisibility(View.GONE);
            mMorphButtonUtility.morphToGrey(mCreatePdf, mMorphButtonUtility.integer());
        }

        return root;
    }

    private void checkForImagesInBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getBoolean("open_select_images"))
                startAddingImages();
        }
    }

    @OnClick(R.id.addImages)
    void startAddingImages() {
        if (!mPermissionGranted) {
            getRuntimePermissions();
            return;
        }
        if (!mIsButtonAlreadyClicked) {
            selectImages();
            mIsButtonAlreadyClicked = true;
        }
    }

    @OnClick(R.id.pdfCreate)
    void pdfCreateClicked() {
        createPdf(false);
    }

    public void showSnackbar(Activity context, int resID) {
        Snackbar.make(Objects.requireNonNull(context).findViewById(android.R.id.content),
                resID, Snackbar.LENGTH_LONG).show();
    }

    public MaterialDialog.Builder createCustomDialog(Activity activity,
                                                     int title, int content) {
        return new MaterialDialog.Builder(activity)
                .title(title)
                .content(content)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel);
    }

    public MaterialDialog.Builder createOverwriteDialog(Activity activity) {
        return new MaterialDialog.Builder(activity)
                .title(R.string.warning)
                .content(R.string.overwrite_message)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel);
    }

    public boolean isEmpty(CharSequence s) {
        return s == null || s.toString().trim().equals("");
    }

    private void createPdf(boolean isGrayScale) {
        String preFillName = mFileUtils.getLastFileName(mImagesUri);
        MaterialDialog.Builder builder = createCustomDialog(mActivity,
                R.string.creating_pdf, R.string.enter_file_name);
        builder.input(getString(R.string.example), preFillName, (dialog, input) -> {
            if (isEmpty(input)) {
                showSnackbar(mActivity, R.string.snackbar_name_not_blank);
            } else {
                final String filename = input.toString();
                FileUtils utils = new FileUtils(mActivity);
                if (!utils.isFileExist(filename + getString(R.string.pdf_ext))) {
                    new CreatePdf(mImagesUri, filename, mHomePath,
                            ImageToPdfFragment.this).execute();
                } else {
                    MaterialDialog.Builder builder2 = createOverwriteDialog(mActivity);
                    builder2.onPositive((dialog2, which) -> {
                        new CreatePdf(mImagesUri, filename, mHomePath, ImageToPdfFragment.this).execute();
                    }).onNegative((dialog1, which) -> createPdf(isGrayScale)).show();
                }
            }
        }).show();
    }

    @OnClick(R.id.pdfOpen)
    void openPdf() {
        mFileUtils.openFile(mPath, FileUtils.FileType.e_PDF);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length < 1)
            return;

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPermissionGranted = true;
                selectImages();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mIsButtonAlreadyClicked = false;
        if (resultCode != Activity.RESULT_OK || data == null)
            return;

        switch (requestCode) {
            case INTENT_REQUEST_GET_IMAGES:
                mImagesUri.clear();
                mUnarrangedImagesUri.clear();
                mImagesUri.addAll(Matisse.obtainPathResult(data));
                mUnarrangedImagesUri.addAll(mImagesUri);
                if (mImagesUri.size() > 0) {
//                    mNoOfImages.setText(String.format(mActivity.getResources()
//                            .getString(R.string.images_selected), mImagesUri.size()));
//                    mNoOfImages.setVisibility(View.VISIBLE);
                    mCreatePdf.setEnabled(true);
                    mCreatePdf.unblockTouch();
                }
                mMorphButtonUtility.morphToSquare(mCreatePdf, mMorphButtonUtility.integer());
                mOpenPdf.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    public void onPDFCreationStarted() {
    }

    @Override
    public void onPDFCreated(boolean success, String path) {
        if (mMaterialDialog != null && mMaterialDialog.isShowing())
            mMaterialDialog.dismiss();
        new DatabaseHelper(mActivity).insertRecord(path, mActivity.getString(R.string.created));
        mOpenPdf.setVisibility(View.VISIBLE);
        mMorphButtonUtility.morphToSuccess(mCreatePdf);
        mCreatePdf.blockTouch();
        mPath = path;
        resetValues();
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
                REQUEST_PERMISSIONS_CODE);
    }

    private void selectImages() {
        Matisse.from(this)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .capture(true)
                .captureStrategy(new CaptureStrategy(true, "com.nabeelbaghoor.shareFile"))
                .maxSelectable(1000)
                .imageEngine(new PicassoEngine())
                .forResult(INTENT_REQUEST_GET_IMAGES);
    }

    private void resetValues() {
        mImagesUri.clear();
//        mNoOfImages.setVisibility(View.GONE);
    }

    public static final String[] READ_WRITE_CAMERA_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int INTENT_REQUEST_GET_IMAGES = 13;
    private static final int REQUEST_PERMISSIONS_CODE = 124;

}
