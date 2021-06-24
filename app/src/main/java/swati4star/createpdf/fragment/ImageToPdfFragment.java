package swati4star.createpdf.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.morphingbutton.MorphingButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import swati4star.createpdf.R;
import swati4star.createpdf.database.DatabaseHelper;
import swati4star.createpdf.interfaces.OnItemClickListener;
import swati4star.createpdf.interfaces.OnPDFCreatedInterface;
import swati4star.createpdf.model.ImageToPDFOptions;
import swati4star.createpdf.util.CreatePdf;
import swati4star.createpdf.util.FileUtils;
import swati4star.createpdf.util.ImageUtils;
import swati4star.createpdf.util.MorphButtonUtility;

//import swati4star.createpdf.util.Constants;
//import swati4star.createpdf.util.DialogUtils;
//import swati4star.createpdf.util.PageSizeUtils;
//import swati4star.createpdf.util.PermissionsUtils;
//import swati4star.createpdf.util.StringUtils;
//
//import static swati4star.createpdf.util.Constants.AUTHORITY_APP;
//import static swati4star.createpdf.util.Constants.DEFAULT_BORDER_WIDTH;
//import static swati4star.createpdf.util.Constants.DEFAULT_COMPRESSION;
//import static swati4star.createpdf.util.Constants.DEFAULT_IMAGE_BORDER_TEXT;
//import static swati4star.createpdf.util.Constants.DEFAULT_IMAGE_SCALE_TYPE_TEXT;
//import static swati4star.createpdf.util.Constants.DEFAULT_PAGE_COLOR;
//import static swati4star.createpdf.util.Constants.DEFAULT_PAGE_SIZE;
//import static swati4star.createpdf.util.Constants.DEFAULT_PAGE_SIZE_TEXT;
//import static swati4star.createpdf.util.Constants.DEFAULT_QUALITY_VALUE;
//import static swati4star.createpdf.util.Constants.IMAGE_SCALE_TYPE_ASPECT_RATIO;
//import static swati4star.createpdf.util.Constants.MASTER_PWD_STRING;
//import static swati4star.createpdf.util.Constants.OPEN_SELECT_IMAGES;
//import static swati4star.createpdf.util.Constants.READ_WRITE_CAMERA_PERMISSIONS;
//import static swati4star.createpdf.util.Constants.RESULT;
//import static swati4star.createpdf.util.Constants.STORAGE_LOCATION;
//import static swati4star.createpdf.util.Constants.appName;

/**
 * ImageToPdfFragment fragment to start with creating PDF
 */
public class ImageToPdfFragment extends Fragment implements OnItemClickListener,
        OnPDFCreatedInterface {

    public static final String[] READ_WRITE_CAMERA_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int INTENT_REQUEST_APPLY_FILTER = 10;
    private static final int INTENT_REQUEST_PREVIEW_IMAGE = 11;
    private static final int INTENT_REQUEST_REARRANGE_IMAGE = 12;
    private static final int INTENT_REQUEST_GET_IMAGES = 13;
    private static final int REQUEST_PERMISSIONS_CODE = 124;
    private static final ArrayList<String> mUnarrangedImagesUri = new ArrayList<>();
    public static ArrayList<String> mImagesUri = new ArrayList<>();
    private final int mMarginTop = 50;
    private final int mMarginBottom = 38;
    private final int mMarginLeft = 50;
    private final int mMarginRight = 38;
    @BindView(R.id.pdfCreate)
    MorphingButton mCreatePdf;
    @BindView(R.id.pdfOpen)
    MorphingButton mOpenPdf;
//    @BindView(R.id.enhancement_options_recycle_view)
//    RecyclerView mEnhancementOptionsRecycleView;
    @BindView(R.id.tvNoOfImages)
    TextView mNoOfImages;
    private MorphButtonUtility mMorphButtonUtility;
    private Activity mActivity;
    private String mPath;
    private SharedPreferences mSharedPreferences;
    private FileUtils mFileUtils;
    //    private PageSizeUtils mPageSizeUtils;
    private int mPageColor;
    private boolean mIsButtonAlreadyClicked = false;
    private ImageToPDFOptions mPdfOptions;
    private MaterialDialog mMaterialDialog;
    private String mHomePath;
    private String mPageNumStyle;
    private int mChoseId;
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
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mPermissionGranted = checkRuntimePermissions(this,
                READ_WRITE_CAMERA_PERMISSIONS);
        mMorphButtonUtility = new MorphButtonUtility(mActivity);
        mFileUtils = new FileUtils(mActivity);
//        mPageSizeUtils = new PageSizeUtils(mActivity);
        mPageColor = Color.WHITE;
        mHomePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PDFfiles/";

        // Get default values & show enhancement options
        resetValues();

        // Check for the images received
        checkForImagesInBundle();

        if (mImagesUri.size() > 0) {
            mNoOfImages.setText(String.format(mActivity.getResources()
                    .getString(R.string.images_selected), mImagesUri.size()));
            mNoOfImages.setVisibility(View.VISIBLE);
            mMorphButtonUtility.morphToSquare(mCreatePdf, mMorphButtonUtility.integer());
            mCreatePdf.setEnabled(true);
//            StringUtils.getInstance().showSnackbar(mActivity, R.string.successToast);
        } else {
            mNoOfImages.setVisibility(View.GONE);
            mMorphButtonUtility.morphToGrey(mCreatePdf, mMorphButtonUtility.integer());
        }

        return root;
    }

    /**
     * Adds images (if any) received in the bundle
     */
    private void checkForImagesInBundle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getBoolean("open_select_images"))
                startAddingImages();
//            ArrayList<Parcelable> uris = bundle.getParcelableArrayList(getString(R.string.bundleKey));
//            if (uris == null)
//                return;
//            for (Parcelable p : uris) {
//                Uri uri = (Uri) p;
//                if (mFileUtils.getUriRealPath(uri) == null) {
//                    StringUtils.getInstance().showSnackbar(mActivity, R.string.whatsappToast);
//                } else {
//                    mImagesUri.add(mFileUtils.getUriRealPath(uri));
//                }
//            }
        }
    }

    /**
     * Adding Images to PDF
     */
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

    /**
     * Create Pdf of selected images
     */
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
        mPdfOptions.setImagesUri(mImagesUri);
        mPdfOptions.setPageSize("DefaultPageSize");
        mPdfOptions.setImageScaleType(ImageUtils.getInstance().mImageScaleType);
        mPdfOptions.setPageNumStyle(mPageNumStyle);
        mPdfOptions.setMasterPwd("PDF Converter");
        mPdfOptions.setPageColor(mPageColor);

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

                    mPdfOptions.setOutFileName(filename);

                    new CreatePdf(mPdfOptions, mHomePath,
                            ImageToPdfFragment.this).execute();
                } else {
                    MaterialDialog.Builder builder2 = createOverwriteDialog(mActivity);
                    builder2.onPositive((dialog2, which) -> {
                        mPdfOptions.setOutFileName(filename);
                        new CreatePdf(mPdfOptions, mHomePath, ImageToPdfFragment.this).execute();
                    }).onNegative((dialog1, which) -> createPdf(isGrayScale)).show();
                }
            }
        }).show();
    }

    @OnClick(R.id.pdfOpen)
    void openPdf() {
        mFileUtils.openFile(mPath, FileUtils.FileType.e_PDF);
    }


    /**
     * Called after user is asked to grant permissions
     *
     * @param requestCode  REQUEST Code for opening permissions
     * @param permissions  permissions asked to user
     * @param grantResults bool array indicating if permission is granted
     */
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
//                StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_permissions_given);
            }
//            else
//                StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_insufficient_permissions);
        }
    }

    /**
     * Called after Matisse Activity is called
     *
     * @param requestCode REQUEST Code for opening Matisse Activity
     * @param resultCode  result code of the process
     * @param data        Data of the image selected
     */
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
                    mNoOfImages.setText(String.format(mActivity.getResources()
                            .getString(R.string.images_selected), mImagesUri.size()));
                    mNoOfImages.setVisibility(View.VISIBLE);
//                    StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_images_added);
                    mCreatePdf.setEnabled(true);
                    mCreatePdf.unblockTouch();
                }
                mMorphButtonUtility.morphToSquare(mCreatePdf, mMorphButtonUtility.integer());
                mOpenPdf.setVisibility(View.GONE);
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                HashMap<Integer, Uri> croppedImageUris =
                        (HashMap) data.getSerializableExtra(CropImage.CROP_IMAGE_EXTRA_RESULT);

                for (int i = 0; i < mImagesUri.size(); i++) {
                    if (croppedImageUris.get(i) != null) {
                        mImagesUri.set(i, croppedImageUris.get(i).getPath());
//                        StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_imagecropped);
                    }
                }
                break;

//            case INTENT_REQUEST_APPLY_FILTER:
//                mImagesUri.clear();
//                ArrayList<String> mFilterUris = data.getStringArrayListExtra(RESULT);
//                int size = mFilterUris.size() - 1;
//                for (int k = 0; k <= size; k++)
//                    mImagesUri.add(mFilterUris.get(k));
//                break;

//            case INTENT_REQUEST_PREVIEW_IMAGE:
//                mImagesUri = data.getStringArrayListExtra(RESULT);
//                if (mImagesUri.size() > 0) {
//                    mNoOfImages.setText(String.format(mActivity.getResources()
//                            .getString(R.string.images_selected), mImagesUri.size()));
//                } else {
//                    mNoOfImages.setVisibility(View.GONE);
//                    mMorphButtonUtility.morphToGrey(mCreatePdf, mMorphButtonUtility.integer());
//                    mCreatePdf.setEnabled(false);
//                }
//                break;
//
//            case INTENT_REQUEST_REARRANGE_IMAGE:
//                mImagesUri = data.getStringArrayListExtra(RESULT);
//                if (!mUnarrangedImagesUri.equals(mImagesUri) && mImagesUri.size() > 0) {
//                    mNoOfImages.setText(String.format(mActivity.getResources()
//                            .getString(R.string.images_selected), mImagesUri.size()));
//                    StringUtils.getInstance().showSnackbar(mActivity, R.string.images_rearranged);
//                    mUnarrangedImagesUri.clear();
//                    mUnarrangedImagesUri.addAll(mImagesUri);
//                }
//                if (mImagesUri.size() == 0) {
//                    mNoOfImages.setVisibility(View.GONE);
//                    mMorphButtonUtility.morphToGrey(mCreatePdf, mMorphButtonUtility.integer());
//                    mCreatePdf.setEnabled(false);
//                }
//                break;
        }
    }

    @Override
    public void onItemClick(int position) {

        if (mImagesUri.size() == 0) {
//            StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_no_images);
            return;
        }
        switch (position) {
//            case 4:
//                mPageSizeUtils.showPageSizeDialog(false);
//                break;
//            case 5:
//                ImageUtils.getInstance().showImageScaleTypeDialog(mActivity, false);
//                break;
            case 9:
                createPdf(true);
                break;
        }
    }

//    public MaterialDialog createAnimationDialog(Activity activity) {
//        return new MaterialDialog.Builder(activity)
//                .customView(R.layout.lottie_anim_dialog, false)
//                .build();
//    }

    @Override
    public void onPDFCreationStarted() {
//        mMaterialDialog = createAnimationDialog(mActivity);
//        mMaterialDialog.show();
    }

    @Override
    public void onPDFCreated(boolean success, String path) {
        if (mMaterialDialog != null && mMaterialDialog.isShowing())
            mMaterialDialog.dismiss();

        /*if (!success) {
            StringUtils.getInstance().showSnackbar(mActivity, R.string.snackbar_folder_not_created);
            return;
        }*/
        new DatabaseHelper(mActivity).insertRecord(path, mActivity.getString(R.string.created));
//        StringUtils.getInstance().getSnackbarwithAction(mActivity, R.string.snackbar_pdfCreated)
//                .setAction(R.string.snackbar_viewAction,
//                        v -> mFileUtils.openFile(mPath, FileUtils.FileType.e_PDF)).show();
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

    /**
     * Opens Matisse activity to select Images
     */
    private void selectImages() {
        Matisse.from(this)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .capture(true)
                .captureStrategy(new CaptureStrategy(true, "com.swati4star.shareFile"))
                .maxSelectable(1000)
                .imageEngine(new PicassoEngine())
                .forResult(INTENT_REQUEST_GET_IMAGES);
    }

    /**
     * Resets pdf creation related values & show enhancement options
     */
    private void resetValues() {
        mPdfOptions = new ImageToPDFOptions();
        mPdfOptions.setBorderWidth(0);
        mPdfOptions.setQualityString(
                Integer.toString(30));
        mPdfOptions.setPageSize("A4");
        mPdfOptions.setPasswordProtected(false);
        mPdfOptions.setWatermarkAdded(false);
        mImagesUri.clear();
        mNoOfImages.setVisibility(View.GONE);
        ImageUtils.getInstance().mImageScaleType = "maintain_aspect_ratio";
        mPdfOptions.setMargins(0, 0, 0, 0);
        mPageNumStyle = "pref_page_number_style";
        mPageColor = Color.WHITE;
    }
}
